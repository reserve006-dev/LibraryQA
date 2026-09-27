package library;

// Тип читача (замість «магічних» кодів 1, 2, 3)
public enum ReaderType {
    STUDENT("Студент"),
    TEACHER("Викладач"),
    GUEST("Гість");

    private final String title;

    ReaderType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
