package library;

// Гість: до 2 книг, термін 7 днів, штраф 5.0 грн за день прострочення
public final class GuestReader extends Reader {

    public static final int MAX_BOOKS = 2;
    public static final int LOAN_DAYS = 7;
    public static final double FINE_PER_DAY = 5.0;

    public GuestReader(int id, String name, int age) {
        super(id, name, age);
    }

    @Override
    public ReaderType getType() {
        return ReaderType.GUEST;
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
