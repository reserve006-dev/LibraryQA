package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Основні операції бібліотеки: облік книг і читачів, видача, повернення, оплата штрафів
public class LibraryService {

    private final Map<Integer, Book> books = new HashMap<>();
    private final Map<Integer, Reader> readers = new HashMap<>();
    private final Map<Integer, Loan> loansByBookId = new HashMap<>();
    private final FineCalculator fineCalculator = new FineCalculator();

    private int nextBookId = 0;
    private int nextReaderId = 0;

    public synchronized Book addBook(String title, String author, int year) {
        Book book = new Book(nextBookId, title, author, year);
        books.put(book.getId(), book);
        nextBookId++;
        return book;
    }

    public synchronized Reader registerReader(ReaderType type, String name, int age) {
        Reader reader = Reader.create(type, nextReaderId, name, age);
        readers.put(reader.getId(), reader);
        nextReaderId++;
        return reader;
    }

    public synchronized IssueStatus issueBook(int readerId, int bookId, LocalDate date) {
        Reader reader = readers.get(readerId);
        if (reader == null) {
            return IssueStatus.READER_NOT_REGISTERED;
        }
        Book book = books.get(bookId);
        if (book == null || !book.isAvailable()) {
            return IssueStatus.BOOK_NOT_AVAILABLE;
        }
        if (reader.hasDebt()) {
            return IssueStatus.HAS_DEBT;
        }
        if (!reader.canTakeMoreBooks()) {
            return IssueStatus.LIMIT_REACHED;
        }
        openLoan(reader, book, date);
        return IssueStatus.ISSUED;
    }

    public synchronized ReturnResult returnBook(int readerId, int bookId, LocalDate date, boolean damaged) {
        Loan loan = loansByBookId.get(bookId);
        if (loan == null || loan.getReader().getId() != readerId) {
            return ReturnResult.notIssued();
        }
        if (date.isBefore(loan.getIssueDate())) {
            throw new LibraryException("Дата повернення раніше дати видачі");
        }
        long overdueDays = fineCalculator.overdueDays(loan.getDueDate(), date);
        double overdueFine = fineCalculator.overdueFine(loan.getReader(), overdueDays);
        double damageFine = fineCalculator.damageFine(damaged);

        closeLoan(loan);
        loan.getReader().addDebt(overdueFine + damageFine);
        return ReturnResult.accepted(overdueFine, damageFine);
    }

    public synchronized double payFine(int readerId, double amount) {
        Reader reader = readers.get(readerId);
        if (reader == null) {
            throw new LibraryException("Читач не зареєстрований");
        }
        if (amount <= 0) {
            throw new LibraryException("Сума має бути додатною");
        }
        return reader.pay(amount);
    }

    private void openLoan(Reader reader, Book book, LocalDate date) {
        book.setAvailable(false);
        reader.loanStarted();
        loansByBookId.put(book.getId(), new Loan(reader, book, date));
    }

    private void closeLoan(Loan loan) {
        loan.getBook().setAvailable(true);
        loan.getReader().loanFinished();
        loansByBookId.remove(loan.getBook().getId());
    }

    public synchronized Reader findReader(int readerId) {
        return readers.get(readerId);
    }

    public synchronized Book findBook(int bookId) {
        return books.get(bookId);
    }

    public synchronized Loan findLoan(int bookId) {
        return loansByBookId.get(bookId);
    }

    public synchronized int getActiveLoanCount() {
        return loansByBookId.size();
    }

    public synchronized int getBookCount() {
        return books.size();
    }

    public synchronized List<Reader> getReaders() {
        return new ArrayList<>(readers.values());
    }
}
