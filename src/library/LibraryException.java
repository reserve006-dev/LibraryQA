package library;

// Виняток предметної області: некоректні дані або недопустима операція
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
