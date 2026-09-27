package library;

import java.time.Year;

// Книга бібліотечного фонду
public class Book {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_AUTHOR_LENGTH = 60;
    public static final int MIN_YEAR = 1450;

    private final int id;
    private final String title;
    private final String author;
    private final int year;
    private boolean available = true;

    public Book(int id, String title, String author, int year) {
        this.id = id;
        this.title = requireText(title, MAX_TITLE_LENGTH, "Некоректна назва книги");
        this.author = requireText(author, MAX_AUTHOR_LENGTH, "Некоректне ім'я автора");
        this.year = validateYear(year);
    }

    public static int maxYear() {
        return Year.now().getValue();
    }

    private static String requireText(String value, int maxLength, String errorMessage) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength) {
            throw new LibraryException(errorMessage);
        }
        return value.trim();
    }

    private static int validateYear(int year) {
        if (year < MIN_YEAR || year > maxYear()) {
            throw new LibraryException("Некоректний рік видання");
        }
        return year;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    public boolean isAvailable() {
        return available;
    }

    void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("%d. «%s», %s, %d", id, title, author, year);
    }
}
