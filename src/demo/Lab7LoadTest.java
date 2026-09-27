package demo;

import library.IssueStatus;
import library.LibraryService;
import library.ReaderType;
import library.ReturnResult;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

// Лабораторна робота №7: навантажувальне тестування сервісу бібліотеки
public class Lab7LoadTest {

    private static final LocalDate DAY = LocalDate.of(2026, 9, 1);
    private static final int TEST_SECONDS = 3;
    private static final int SAMPLES_PER_THREAD = 200_000;

    public static void main(String[] args) throws Exception {
        System.out.println("НАВАНТАЖУВАЛЬНЕ ТЕСТУВАННЯ СИСТЕМИ «БІБЛІОТЕКА»");
        System.out.println("Ядер процесора: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        System.out.println("Розігрів JVM (результати не враховуються)...");
        throughputTest(2, 1);
        System.out.println();

        testThroughputByThreads();
        testCatalogSize();
        testContention();
    }

    // Тест 1: пропускна здатність і час відгуку залежно від кількості потоків
    private static void testThroughputByThreads() throws Exception {
        System.out.println("ТЕСТ 1. Пропускна здатність (операція = видача + повернення книги)");
        System.out.printf("%-8s %-14s %-14s %-16s %-16s %-16s%n",
                "Потоків", "Операцій", "Операцій/с", "Середній, мкс", "95%, мкс", "Макс., мкс");
        for (int threads = 1; threads <= 8; threads *= 2) {
            Result r = throughputTest(TEST_SECONDS, threads);
            System.out.printf("%-8d %-14d %-14d %-16.2f %-16.2f %-16.2f%n",
                    threads, r.operations, r.operations / TEST_SECONDS,
                    r.avgMicros, r.p95Micros, r.maxMicros);
        }
        System.out.println();
    }

    private static Result throughputTest(int seconds, int threads) throws Exception {
        final LibraryService service = new LibraryService();
        final int booksPerThread = 50;
        for (int t = 0; t < threads; t++) {
            service.registerReader(ReaderType.TEACHER, "Читач " + t, 30);
            for (int b = 0; b < booksPerThread; b++) {
                service.addBook("Книга " + t + "-" + b, "Автор", 2000);
            }
        }
        final long endTime = System.nanoTime() + seconds * 1_000_000_000L;
        final CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        Future<?>[] futures = new Future<?>[threads];
        final long[][] latencies = new long[threads][SAMPLES_PER_THREAD];
        final int[] counts = new int[threads];
        final long[] operations = new long[threads];

        for (int t = 0; t < threads; t++) {
            final int readerId = t;
            futures[t] = pool.submit(() -> {
                startSignal.await();
                int bookId = readerId * booksPerThread;
                int i = 0;
                while (System.nanoTime() < endTime) {
                    int book = bookId + (i % booksPerThread);
                    long begin = System.nanoTime();
                    IssueStatus status = service.issueBook(readerId, book, DAY);
                    ReturnResult result = service.returnBook(readerId, book, DAY, false);
                    long spent = System.nanoTime() - begin;
                    if (status != IssueStatus.ISSUED || !result.isAccepted()) {
                        throw new IllegalStateException("Помилка обробки операції");
                    }
                    if (counts[readerId] < SAMPLES_PER_THREAD) {
                        latencies[readerId][counts[readerId]++] = spent;
                    }
                    operations[readerId]++;
                    i++;
                }
                return null;
            });
        }
        startSignal.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        int totalSamples = 0;
        long totalOps = 0;
        for (int t = 0; t < threads; t++) {
            totalSamples += counts[t];
            totalOps += operations[t];
        }
        long[] all = new long[totalSamples];
        int pos = 0;
        for (int t = 0; t < threads; t++) {
            System.arraycopy(latencies[t], 0, all, pos, counts[t]);
            pos += counts[t];
        }
        Arrays.sort(all);
        double sum = 0;
        for (long v : all) {
            sum += v;
        }
        Result r = new Result();
        r.operations = totalOps;
        r.avgMicros = sum / all.length / 1000.0;
        r.p95Micros = all[(int) (all.length * 0.95)] / 1000.0;
        r.maxMicros = all[all.length - 1] / 1000.0;
        return r;
    }

    // Тест 2: чи залежить швидкість операцій від обсягу фонду бібліотеки
    private static void testCatalogSize() {
        System.out.println("ТЕСТ 2. Залежність від обсягу фонду (100 000 операцій видачі + повернення)");
        System.out.printf("%-14s %-24s %-22s %-16s%n",
                "Книг у фонді", "Час заповнення, мс", "Час операцій, мс", "Операцій/с");
        int[] sizes = {1_000, 10_000, 100_000, 500_000};
        int ops = 100_000;
        for (int size : sizes) {
            LibraryService service = new LibraryService();
            long fillStart = System.nanoTime();
            for (int i = 0; i < size; i++) {
                service.addBook("Книга " + i, "Автор " + (i % 100), 1950 + i % 70);
            }
            service.registerReader(ReaderType.TEACHER, "Викладач", 40);
            long fillMs = (System.nanoTime() - fillStart) / 1_000_000;

            long start = System.nanoTime();
            for (int i = 0; i < ops; i++) {
                int bookId = (int) ((i * 7919L) % size);
                service.issueBook(0, bookId, DAY);
                service.returnBook(0, bookId, DAY, false);
            }
            long ms = Math.max(1, (System.nanoTime() - start) / 1_000_000);
            System.out.printf("%-14d %-24d %-22d %-16d%n", size, fillMs, ms, ops * 1000L / ms);
        }
        System.out.println();
    }

    // Тест 3: конкуренція — багато читачів одночасно намагаються взяти ті самі книги
    private static void testContention() throws Exception {
        System.out.println("ТЕСТ 3. Конкуренція: 16 потоків, 200 читачів, лише 20 книг");
        final LibraryService service = new LibraryService();
        final int readers = 200;
        final int books = 20;
        for (int i = 0; i < readers; i++) {
            service.registerReader(ReaderType.STUDENT, "Студент " + i, 20);
        }
        for (int i = 0; i < books; i++) {
            service.addBook("Популярна книга " + i, "Автор", 2020);
        }
        final AtomicInteger issued = new AtomicInteger();
        final AtomicInteger rejected = new AtomicInteger();
        final AtomicInteger returned = new AtomicInteger();
        final int attemptsPerThread = 100_000;
        final int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        Future<?>[] futures = new Future<?>[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int seed = t;
            futures[t] = pool.submit(() -> {
                for (int i = 0; i < attemptsPerThread; i++) {
                    int reader = (seed * 31 + i * 17) % readers;
                    int book = (seed + i) % books;
                    IssueStatus status = service.issueBook(reader, book, DAY);
                    if (status == IssueStatus.ISSUED) {
                        issued.incrementAndGet();
                        if (service.returnBook(reader, book, DAY, false).isAccepted()) {
                            returned.incrementAndGet();
                        }
                    } else {
                        rejected.incrementAndGet();
                    }
                }
                return null;
            });
        }
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();
        long ms = Math.max(1, (System.nanoTime() - start) / 1_000_000);

        long attempts = (long) threads * attemptsPerThread;
        System.out.println("Спроб видачі:                 " + attempts);
        System.out.println("Успішних видач:               " + issued.get());
        System.out.println("Відмов (книга зайнята):       " + rejected.get());
        System.out.println("Успішних повернень:           " + returned.get());
        System.out.println("Час виконання, мс:            " + ms);
        System.out.println("Спроб за секунду:             " + attempts * 1000L / ms);
        boolean consistent = issued.get() == returned.get()
                && service.getActiveLoanCount() == 0
                && issued.get() + rejected.get() == attempts;
        System.out.println("Цілісність даних збережена:   " + (consistent ? "ТАК" : "НІ"));
    }

    private static final class Result {
        long operations;
        double avgMicros;
        double p95Micros;
        double maxMicros;
    }
}
