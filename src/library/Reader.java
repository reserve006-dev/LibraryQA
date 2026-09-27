package library;

// Читач бібліотеки. Правила, що залежать від типу читача,
// визначаються у підкласах (заміна умовного оператора поліморфізмом)
public abstract class Reader {

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 40;
    public static final int MIN_AGE = 6;
    public static final int MAX_AGE = 100;

    private final int id;
    private final String name;
    private final int age;
    private double debt;
    private int activeLoans;

    protected Reader(int id, String name, int age) {
        this.id = id;
        this.name = validateName(name);
        this.age = validateAge(age);
    }

    // Фабричний метод (введення фабрики)
    public static Reader create(ReaderType type, int id, String name, int age) {
        if (type == null) {
            throw new LibraryException("Невідомий тип читача");
        }
        switch (type) {
            case STUDENT:
                return new StudentReader(id, name, age);
            case TEACHER:
                return new TeacherReader(id, name, age);
            default:
                return new GuestReader(id, name, age);
        }
    }

    public abstract ReaderType getType();

    public abstract int getMaxBooks();

    public abstract int getLoanDays();

    public abstract double getFinePerDay();

    private static String validateName(String name) {
        if (name == null) {
            throw new LibraryException("Некоректне ім'я читача");
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            throw new LibraryException("Некоректне ім'я читача");
        }
        return trimmed;
    }

    private static int validateAge(int age) {
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new LibraryException("Некоректний вік читача");
        }
        return age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getDebt() {
        return debt;
    }

    public boolean hasDebt() {
        return debt > 0;
    }

    public int getActiveLoans() {
        return activeLoans;
    }

    public boolean canTakeMoreBooks() {
        return activeLoans < getMaxBooks();
    }

    void addDebt(double amount) {
        debt += amount;
    }

    double pay(double amount) {
        debt = Math.max(0, debt - amount);
        return debt;
    }

    void loanStarted() {
        activeLoans++;
    }

    void loanFinished() {
        activeLoans--;
    }

    @Override
    public String toString() {
        return String.format("%d. %s (%s), книг на руках: %d, борг: %.2f грн",
                id, name, getType().getTitle(), activeLoans, debt);
    }
}
