package library;

// Студент: до 5 книг, термін 14 днів, штраф 2.0 грн за день прострочення
public final class StudentReader extends Reader {

    public static final int MAX_BOOKS = 5;
    public static final int LOAN_DAYS = 14;
    public static final double FINE_PER_DAY = 2.0;

    public StudentReader(int id, String name, int age) {
        super(id, name, age);
    }

    @Override
    public ReaderType getType() {
        return ReaderType.STUDENT;
    }

    @Override
    public int getMaxBooks() {
        return MAX_BOOKS;
    }

    @Override
    public int getLoanDays() {
        return LOAN_DAYS;
    }

    @Override
    public double getFinePerDay() {
        return FINE_PER_DAY;
    }
}
