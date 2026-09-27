package library;

// Викладач: до 10 книг, термін 30 днів, штраф 1.0 грн за день прострочення
public final class TeacherReader extends Reader {

    public static final int MAX_BOOKS = 10;
    public static final int LOAN_DAYS = 30;
    public static final double FINE_PER_DAY = 1.0;

    public TeacherReader(int id, String name, int age) {
        super(id, name, age);
    }

    @Override
    public ReaderType getType() {
        return ReaderType.TEACHER;
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
