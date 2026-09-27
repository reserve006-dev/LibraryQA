package library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Лабораторна робота №6. Тестування методом аналізу граничних значень.
// Для кожної межі перевіряються три значення: перед межею, на межі, після межі.
class Lab6BoundaryValueTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 9, 1);

    private static String nameOfLength(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('я');
        }
        return sb.toString();
    }

    @Nested
    @DisplayName("Вік читача: межі 6 та 100")
    class ReaderAge {
        @ParameterizedTest(name = "вік {0} -> допустимий: {1}")
        @CsvSource({
                "5,   false",
                "6,   true",
                "7,   true",
                "99,  true",
                "100, true",
                "101, false"
        })
        void age(int age, boolean valid) {
            LibraryService service = new LibraryService();
            if (valid) {
                assertEquals(age, service.registerReader(ReaderType.STUDENT, "Олена", age).getAge());
            } else {
                LibraryException e = assertThrows(LibraryException.class,
                        () -> service.registerReader(ReaderType.STUDENT, "Олена", age));
                assertEquals("Некоректний вік читача", e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Довжина імені читача: межі 2 та 40 символів")
    class ReaderNameLength {
        @ParameterizedTest(name = "довжина {0} -> допустима: {1}")
        @CsvSource({
                "1,  false",
                "2,  true",
                "3,  true",
                "39, true",
                "40, true",
                "41, false"
        })
        void nameLength(int length, boolean valid) {
            LibraryService service = new LibraryService();
            String name = nameOfLength(length);
            if (valid) {
                assertEquals(name, service.registerReader(ReaderType.GUEST, name, 30).getName());
            } else {
                assertThrows(LibraryException.class,
                        () -> service.registerReader(ReaderType.GUEST, name, 30));
            }
        }
    }

    @Nested
    @DisplayName("Рік видання книги: межі 1450 та поточний рік")
    class BookYear {
        @ParameterizedTest(name = "рік MIN{0} -> допустимий: {1}")
        @CsvSource({"-1, false", "0, true", "1, true"})
        void lowerBound(int shift, boolean valid) {
            checkYear(Book.MIN_YEAR + shift, valid);
        }

        @ParameterizedTest(name = "рік MAX{0} -> допустимий: {1}")
        @CsvSource({"-1, true", "0, true", "1, false"})
        void upperBound(int shift, boolean valid) {
            checkYear(Book.maxYear() + shift, valid);
        }

        private void checkYear(int year, boolean valid) {
            if (valid) {
                assertEquals(year, new Book(0, "Книга", "Автор", year).getYear());
            } else {
                assertThrows(LibraryException.class, () -> new Book(0, "Книга", "Автор", year));
            }
        }
    }

    @Nested
    @DisplayName("Ліміт книг на руках: студент 5, викладач 10, гість 2")
    class LoanLimit {
        @ParameterizedTest(name = "{0}: вже на руках {1} -> {2}")
        @CsvSource({
                "STUDENT, 4,  ISSUED",
                "STUDENT, 5,  LIMIT_REACHED",
                "STUDENT, 6,  LIMIT_REACHED",
                "TEACHER, 9,  ISSUED",
                "TEACHER, 10, LIMIT_REACHED",
                "TEACHER, 11, LIMIT_REACHED",
                "GUEST,   1,  ISSUED",
                "GUEST,   2,  LIMIT_REACHED",
                "GUEST,   3,  LIMIT_REACHED"
        })
        void limit(ReaderType type, int alreadyTaken, IssueStatus expected) {
            LibraryService service = new LibraryService();
            Reader reader = service.registerReader(type, "Читач", 30);
            for (int i = 0; i <= alreadyTaken; i++) {
                service.addBook("Книга " + i, "Автор", 2000);
            }
            int taken = 0;
            for (int i = 0; i < alreadyTaken; i++) {
                if (service.issueBook(reader.getId(), i, ISSUE_DATE) == IssueStatus.ISSUED) {
                    taken++;
                }
            }
            assertEquals(Math.min(alreadyTaken, reader.getMaxBooks()), taken);
            assertEquals(expected, service.issueBook(reader.getId(), alreadyTaken, ISSUE_DATE));
        }
    }

    @Nested
    @DisplayName("Термін повернення (студент, 14 днів): межа – день повернення")
    class DueDate {
        @ParameterizedTest(name = "повернення на {0}-й день -> штраф {1} грн")
        @CsvSource({
                "13, 0.0",
                "14, 0.0",
                "15, 2.0"
        })
        void overdueBoundary(int daysAfterIssue, double expectedFine) {
            LibraryService service = new LibraryService();
            Reader reader = service.registerReader(ReaderType.STUDENT, "Олена", 19);
            Book book = service.addBook("Кобзар", "Тарас Шевченко", 1840);
            service.issueBook(reader.getId(), book.getId(), ISSUE_DATE);
            ReturnResult result = service.returnBook(reader.getId(), book.getId(),
                    ISSUE_DATE.plusDays(daysAfterIssue), false);
            assertEquals(expectedFine, result.getOverdueFine(), 0.0001);
        }
    }

    @Nested
    @DisplayName("Дата повернення: межа – дата видачі")
    class ReturnDate {
        @Test
        @DisplayName("на день раніше дати видачі -> помилка")
        void dayBeforeIssue() {
            LibraryService service = prepared();
            assertThrows(LibraryException.class,
                    () -> service.returnBook(0, 0, ISSUE_DATE.minusDays(1), false));
        }

        @Test
        @DisplayName("у день видачі -> книгу прийнято")
        void sameDay() {
            assertTrue(prepared().returnBook(0, 0, ISSUE_DATE, false).isAccepted());
        }

        @Test
        @DisplayName("наступного дня -> книгу прийнято")
        void dayAfterIssue() {
            assertTrue(prepared().returnBook(0, 0, ISSUE_DATE.plusDays(1), false).isAccepted());
        }

        private LibraryService prepared() {
            LibraryService service = new LibraryService();
            service.registerReader(ReaderType.STUDENT, "Олена", 19);
            service.addBook("Кобзар", "Тарас Шевченко", 1840);
            service.issueBook(0, 0, ISSUE_DATE);
            return service;
        }
    }

    @Nested
    @DisplayName("Максимальний штраф за прострочення: 100 грн")
    class FineCap {
        private final FineCalculator calculator = new FineCalculator();

        @ParameterizedTest(name = "студент, {0} дн. прострочення -> {1} грн")
        @CsvSource({
                "49, 98.0",
                "50, 100.0",
                "51, 100.0"
        })
        void studentCap(long days, double expected) {
            Reader student = new StudentReader(0, "Олена", 19);
            assertEquals(expected, calculator.overdueFine(student, days), 0.0001);
        }

        @ParameterizedTest(name = "гість, {0} дн. прострочення -> {1} грн")
        @CsvSource({
                "19, 95.0",
                "20, 100.0",
                "21, 100.0"
        })
        void guestCap(long days, double expected) {
            Reader guest = new GuestReader(0, "Марія", 30);
            assertEquals(expected, calculator.overdueFine(guest, days), 0.0001);
        }

        @ParameterizedTest(name = "{0} дн. прострочення -> допустимо: {1}")
        @CsvSource({"-1, false", "0, true", "1, true"})
        void daysLowerBound(long days, boolean valid) {
            Reader student = new StudentReader(0, "Олена", 19);
            if (valid) {
                assertEquals(days * StudentReader.FINE_PER_DAY, calculator.overdueFine(student, days), 0.0001);
            } else {
                assertThrows(LibraryException.class, () -> calculator.overdueFine(student, days));
            }
        }
    }
}
