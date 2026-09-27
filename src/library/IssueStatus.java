package library;

// Результат спроби видати книгу
public enum IssueStatus {
    READER_NOT_REGISTERED("Читач не зареєстрований"),
    BOOK_NOT_AVAILABLE("Книга недоступна"),
    HAS_DEBT("Спочатку погасіть заборгованість"),
    LIMIT_REACHED("Перевищено ліміт книг для читача"),
    ISSUED("Книгу видано");

    private final String message;

    IssueStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
