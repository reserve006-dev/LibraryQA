package library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Лабораторна робота №4. Тестування методом функціональних діаграм
// (діаграм причинно-наслідкових зв'язків). Модуль «Повернення книги».
// Причини:
//   1 – книга видавалась саме цьому читачу;
//   2 – книгу повернено вчасно (не пізніше терміну повернення);
//   3 – книга пошкоджена.
// Наслідки:
//   20 – повідомлення «Книга не видавалась цьому читачу»   (20 = ¬1)
//   21 – книгу прийнято до фонду                          (21 = 1)
//   22 – нараховано штраф за прострочення                 (22 = 1 ∧ ¬2)
//   23 – нараховано штраф за пошкодження                  (23 = 1 ∧ 3)
class Lab4ReturnBookCauseEffectTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate DUE_DATE = ISSUE_DATE.plusDays(StudentReader.LOAN_DAYS);
    private static final LocalDate LATE_DATE = DUE_DATE.plusDays(3);

    private LibraryService service;
    private Reader student;
    private Reader otherReader;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
        student = service.registerReader(ReaderType.STUDENT, "Олена Коваль", 19);
        otherReader = service.registerReader(ReaderType.STUDENT, "Андрій Мельник", 20);
        book = service.addBook("Лісова пісня", "Леся Українка", 1911);
        service.issueBook(student.getId(), book.getId(), ISSUE_DATE);
    }

    @Test
    @DisplayName("Тест 1: причина ¬1 -> наслідок 20 (книга не видавалась цьому читачу)")
    void test1_notIssuedToThisReader() {
        ReturnResult result = service.returnBook(otherReader.getId(), book.getId(), DUE_DATE, true);

        assertFalse(result.isAccepted(), "21 не повинен виконуватися");
        assertEquals("Книга не видавалась цьому читачу", result.getMessage());
        assertFalse(result.hasOverdueFine(), "22 не повинен виконуватися");
        assertFalse(result.hasDamageFine(), "23 не повинен виконуватися");
        assertFalse(book.isAvailable(), "Книга залишається у першого читача");
        assertEquals(0.0, otherReader.getDebt());
    }

    @Test
    @DisplayName("Тест 2: причини 1, 2, ¬3 -> наслідок 21 (прийнято без штрафів)")
    void test2_onTimeNotDamaged() {
        ReturnResult result = service.returnBook(student.getId(), book.getId(), DUE_DATE, false);

        assertTrue(result.isAccepted());
        assertFalse(result.hasOverdueFine());
        assertFalse(result.hasDamageFine());
        assertEquals("Книгу прийнято", result.getMessage());
        assertTrue(book.isAvailable());
        assertEquals(0.0, student.getDebt());
    }

    @Test
    @DisplayName("Тест 3: причини 1, ¬2, ¬3 -> наслідки 21, 22")
    void test3_lateNotDamaged() {
        ReturnResult result = service.returnBook(student.getId(), book.getId(), LATE_DATE, false);

        assertTrue(result.isAccepted());
        assertTrue(result.hasOverdueFine());
        assertFalse(result.hasDamageFine());
        assertEquals(3 * StudentReader.FINE_PER_DAY, result.getOverdueFine());
        assertEquals(6.0, student.getDebt());
    }

    @Test
    @DisplayName("Тест 4: причини 1, 2, 3 -> наслідки 21, 23")
    void test4_onTimeDamaged() {
        ReturnResult result = service.returnBook(student.getId(), book.getId(), DUE_DATE, true);

        assertTrue(result.isAccepted());
        assertFalse(result.hasOverdueFine());
        assertTrue(result.hasDamageFine());
        assertEquals(FineCalculator.DAMAGE_FINE, result.getDamageFine());
        assertEquals(50.0, student.getDebt());
    }

    @Test
    @DisplayName("Тест 5: причини 1, ¬2, 3 -> наслідки 21, 22, 23")
    void test5_lateAndDamaged() {
        ReturnResult result = service.returnBook(student.getId(), book.getId(), LATE_DATE, true);

        assertTrue(result.isAccepted());
        assertTrue(result.hasOverdueFine());
        assertTrue(result.hasDamageFine());
        assertEquals(56.0, result.getTotalFine());
        assertEquals("Книгу прийнято, нараховано штраф " + String.format("%.2f", 56.0) + " грн",
                result.getMessage());
        assertEquals(56.0, student.getDebt());
    }

    @Test
    @DisplayName("Тест 6: повторне повернення тієї самої книги -> наслідок 20")
    void test6_returnTwice() {
        service.returnBook(student.getId(), book.getId(), DUE_DATE, false);
        ReturnResult second = service.returnBook(student.getId(), book.getId(), DUE_DATE, false);
        assertFalse(second.isAccepted());
    }
}
