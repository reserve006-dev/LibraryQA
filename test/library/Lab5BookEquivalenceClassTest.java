package library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Лабораторна робота №5. Тестування методом класів еквівалентності.
// Функція «Додавання книги до фонду»: назва, автор, рік видання.
//   Назва:  КЕ1 – 1..100 символів (допустимий); КЕ2 – null; КЕ3 – порожня / лише пробіли; КЕ4 – понад 100 символів.
//   Автор:  КЕ5 – 1..60 символів (допустимий);  КЕ6 – null; КЕ7 – порожній / лише пробіли; КЕ8 – понад 60 символів.
//   Рік:    КЕ9 – від 1450 до поточного року (допустимий); КЕ10 – менше 1450; КЕ11 – більше поточного року.
class Lab5BookEquivalenceClassTest {

    private static final String VALID_TITLE = "Захар Беркут";
    private static final String VALID_AUTHOR = "Іван Франко";
    private static final int VALID_YEAR = 1883;

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Test
    @DisplayName("КЕ1 + КЕ5 + КЕ9: усі дані допустимі – книгу додано")
    void validBook() {
        LibraryService service = new LibraryService();
        Book book = service.addBook(VALID_TITLE, VALID_AUTHOR, VALID_YEAR);
        assertEquals(VALID_TITLE, book.getTitle());
        assertEquals(VALID_AUTHOR, book.getAuthor());
        assertEquals(VALID_YEAR, book.getYear());
        assertTrue(book.isAvailable());
        assertEquals(1, service.getBookCount());
    }

    @Test
    @DisplayName("КЕ1: зайві пробіли навколо назви видаляються")
    void titleIsTrimmed() {
        Book book = new Book(0, "   Кобзар  ", VALID_AUTHOR, 1840);
        assertEquals("Кобзар", book.getTitle());
    }

    static Stream<Arguments> invalidClasses() {
        return Stream.of(
                Arguments.of("КЕ2: назва null", null, VALID_AUTHOR, VALID_YEAR, "Некоректна назва книги"),
                Arguments.of("КЕ3: назва з пробілів", "   ", VALID_AUTHOR, VALID_YEAR, "Некоректна назва книги"),
                Arguments.of("КЕ4: назва 150 символів", repeat('Н', 150), VALID_AUTHOR, VALID_YEAR, "Некоректна назва книги"),
                Arguments.of("КЕ6: автор null", VALID_TITLE, null, VALID_YEAR, "Некоректне ім'я автора"),
                Arguments.of("КЕ7: автор порожній", VALID_TITLE, "", VALID_YEAR, "Некоректне ім'я автора"),
                Arguments.of("КЕ8: автор 90 символів", VALID_TITLE, repeat('А', 90), VALID_YEAR, "Некоректне ім'я автора"),
                Arguments.of("КЕ10: рік 1000", VALID_TITLE, VALID_AUTHOR, 1000, "Некоректний рік видання"),
                Arguments.of("КЕ11: рік 2100", VALID_TITLE, VALID_AUTHOR, 2100, "Некоректний рік видання")
        );
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Недопустимі класи еквівалентності – книгу не додано")
    @MethodSource("invalidClasses")
    void invalidBook(String caseName, String title, String author, int year, String expectedMessage) {
        LibraryService service = new LibraryService();
        LibraryException e = assertThrows(LibraryException.class,
                () -> service.addBook(title, author, year));
        assertEquals(expectedMessage, e.getMessage());
        assertEquals(0, service.getBookCount(), "Некоректна книга не повинна потрапити до фонду");
    }
}
