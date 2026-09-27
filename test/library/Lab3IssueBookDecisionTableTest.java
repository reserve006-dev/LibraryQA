package library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Лабораторна робота №3. Тестування за допомогою таблиць рішень.
// UseCase «Видача книги». Умови:
//   x1 – читач зареєстрований; x2 – книга доступна; x3 – у читача немає заборгованості.
// Дії:
//   «Читач не зареєстрований» = ¬x1; «Книга недоступна» = x1·¬x2;
//   «Спочатку погасіть заборгованість» = x1·x2·¬x3; «Видати книгу» = x1·x2·x3.
class Lab3IssueBookDecisionTableTest {

    private static final LocalDate DAY = LocalDate.of(2026, 9, 1);
    private static final int UNKNOWN_READER_ID = 999;

    private LibraryService service;
    private Reader reader;
    private Reader otherReader;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
        reader = service.registerReader(ReaderType.STUDENT, "Олена Коваль", 19);
        otherReader = service.registerReader(ReaderType.TEACHER, "Петро Іванюк", 45);
        book = service.addBook("Кобзар", "Тарас Шевченко", 1840);
    }

    // Приводить систему до потрібної комбінації умов і виконує видачу
    private IssueStatus issueWith(boolean registered, boolean available, boolean noDebt) {
        if (!available) {
            service.issueBook(otherReader.getId(), book.getId(), DAY);
        }
        if (!noDebt) {
            reader.addDebt(20.0);
        }
        int readerId = registered ? reader.getId() : UNKNOWN_READER_ID;
        return service.issueBook(readerId, book.getId(), DAY);
    }

    // Повна таблиця рішень: 2^3 = 8 стовпців
    @ParameterizedTest(name = "Стовпець {0}: x1={1}, x2={2}, x3={3} -> {4}")
    @DisplayName("Повна таблиця рішень")
    @CsvSource({
            "0, false, false, false, READER_NOT_REGISTERED",
            "1, false, false, true,  READER_NOT_REGISTERED",
            "2, false, true,  false, READER_NOT_REGISTERED",
            "3, false, true,  true,  READER_NOT_REGISTERED",
            "4, true,  false, false, BOOK_NOT_AVAILABLE",
            "5, true,  false, true,  BOOK_NOT_AVAILABLE",
            "6, true,  true,  false, HAS_DEBT",
            "7, true,  true,  true,  ISSUED"
    })
    void fullDecisionTable(int column, boolean registered, boolean available, boolean noDebt,
                           IssueStatus expected) {
        assertEquals(expected, issueWith(registered, available, noDebt));
    }

    // Скорочена таблиця рішень: стовпці 0, 4, 6, 7
    @Test
    @DisplayName("Стовпець 0: читач не зареєстрований (x2, x3 – байдуже)")
    void column0_readerNotRegistered() {
        IssueStatus status = issueWith(false, true, true);
        assertEquals(IssueStatus.READER_NOT_REGISTERED, status);
        assertEquals("Читач не зареєстрований", status.getMessage());
        assertTrue(book.isAvailable(), "Книга має залишитися в бібліотеці");
    }

    @Test
    @DisplayName("Стовпець 4: читач зареєстрований, книга недоступна (x3 – байдуже)")
    void column4_bookNotAvailable() {
        IssueStatus status = issueWith(true, false, true);
        assertEquals(IssueStatus.BOOK_NOT_AVAILABLE, status);
        assertEquals("Книга недоступна", status.getMessage());
        assertEquals(0, reader.getActiveLoans());
    }

    @Test
    @DisplayName("Стовпець 4: книги з таким номером не існує")
    void column4_bookDoesNotExist() {
        assertEquals(IssueStatus.BOOK_NOT_AVAILABLE, service.issueBook(reader.getId(), 12345, DAY));
    }

    @Test
    @DisplayName("Стовпець 6: читач має заборгованість")
    void column6_readerHasDebt() {
        IssueStatus status = issueWith(true, true, false);
        assertEquals(IssueStatus.HAS_DEBT, status);
        assertEquals("Спочатку погасіть заборгованість", status.getMessage());
        assertTrue(book.isAvailable());
    }

    @Test
    @DisplayName("Стовпець 7: усі умови виконані – книгу видано")
    void column7_bookIssued() {
        IssueStatus status = issueWith(true, true, true);
        assertEquals(IssueStatus.ISSUED, status);
        assertEquals("Книгу видано", status.getMessage());
        assertFalse(book.isAvailable());
        assertEquals(1, reader.getActiveLoans());
        assertEquals(DAY.plusDays(StudentReader.LOAN_DAYS), service.findLoan(book.getId()).getDueDate());
    }

    @Test
    @DisplayName("Після оплати боргу книгу видають")
    void issueAfterDebtPaid() {
        reader.addDebt(20.0);
        assertEquals(IssueStatus.HAS_DEBT, service.issueBook(reader.getId(), book.getId(), DAY));
        service.payFine(reader.getId(), 20.0);
        assertEquals(IssueStatus.ISSUED, service.issueBook(reader.getId(), book.getId(), DAY));
    }
}
