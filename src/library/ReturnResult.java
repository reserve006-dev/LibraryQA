package library;

// Результат повернення книги: чи прийнято книгу та які штрафи нараховано
public final class ReturnResult {

    private final boolean accepted;
    private final double overdueFine;
    private final double damageFine;

    private ReturnResult(boolean accepted, double overdueFine, double damageFine) {
        this.accepted = accepted;
        this.overdueFine = overdueFine;
        this.damageFine = damageFine;
    }

    public static ReturnResult notIssued() {
        return new ReturnResult(false, 0, 0);
    }

    public static ReturnResult accepted(double overdueFine, double damageFine) {
        return new ReturnResult(true, overdueFine, damageFine);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public double getOverdueFine() {
        return overdueFine;
    }

    public double getDamageFine() {
        return damageFine;
    }

    public double getTotalFine() {
        return overdueFine + damageFine;
    }

    public boolean hasOverdueFine() {
        return overdueFine > 0;
    }

    public boolean hasDamageFine() {
        return damageFine > 0;
    }

    public String getMessage() {
        if (!accepted) {
            return "Книга не видавалась цьому читачу";
        }
        if (getTotalFine() == 0) {
            return "Книгу прийнято";
        }
        return "Книгу прийнято, нараховано штраф " + String.format("%.2f", getTotalFine()) + " грн";
    }
}
