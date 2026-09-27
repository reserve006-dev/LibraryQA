package library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Розрахунок штрафів (виділений клас)
public class FineCalculator {

    public static final double MAX_OVERDUE_FINE = 100.0;
    public static final double DAMAGE_FINE = 50.0;

    // Кількість днів прострочення (0, якщо книгу повернено вчасно)
    public long overdueDays(LocalDate dueDate, LocalDate returnDate) {
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
    }

    // Штраф за прострочення з урахуванням максимальної суми
    public double overdueFine(Reader reader, long overdueDays) {
        if (overdueDays < 0) {
            throw new LibraryException("Кількість днів прострочення не може бути від'ємною");
        }
        return Math.min(overdueDays * reader.getFinePerDay(), MAX_OVERDUE_FINE);
    }

    public double damageFine(boolean damaged) {
        return damaged ? DAMAGE_FINE : 0.0;
    }
}
