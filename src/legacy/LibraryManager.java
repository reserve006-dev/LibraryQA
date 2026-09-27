package legacy;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

// Облік книг і читачів бібліотеки
// ПОЧАТКОВА ВЕРСІЯ КОДУ (до рефакторингу)
public class LibraryManager {

    // книги
    public ArrayList<String> t = new ArrayList<>();
    public ArrayList<String> a = new ArrayList<>();
    public ArrayList<Integer> y = new ArrayList<>();
    public ArrayList<Boolean> av = new ArrayList<>();

    // читачі
    public ArrayList<String> rn = new ArrayList<>();
    public ArrayList<Integer> age = new ArrayList<>();
    public ArrayList<Integer> rt = new ArrayList<>(); // 1 - студент, 2 - викладач, 3 - гість
    public ArrayList<Double> debt = new ArrayList<>();

    // видачі: {номер читача, номер книги} та дати видачі
    public ArrayList<int[]> loans = new ArrayList<>();
    public ArrayList<LocalDate> ld = new ArrayList<>();

    public int addBook(String title, String author, int year) {
        if (title == null || title.trim().isEmpty() || title.trim().length() > 100) {
            System.out.println("Помилка: некоректна назва книги");
            return -1;
        }
        if (author == null || author.trim().isEmpty() || author.trim().length() > 60) {
            System.out.println("Помилка: некоректне ім'я автора");
            return -1;
        }
        if (year < 1450 || year > Year.now().getValue()) {
            System.out.println("Помилка: некоректний рік видання");
            return -1;
        }
        t.add(title.trim());
        a.add(author.trim());
        y.add(year);
        av.add(true);
        return t.size() - 1;
    }

    public int addReader(String name, int ag, int type) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 40) {
            System.out.println("Помилка: некоректне ім'я читача");
            return -1;
        }
        if (ag < 6 || ag > 100) {
            System.out.println("Помилка: некоректний вік читача");
            return -1;
        }
        if (type != 1 && type != 2 && type != 3) {
            System.out.println("Помилка: невідомий тип читача");
            return -1;
        }
        rn.add(name.trim());
        age.add(ag);
        rt.add(type);
        debt.add(0.0);
        return rn.size() - 1;
    }

    // видача книги
    public String issue(int r, int b, LocalDate d) {
        if (r >= 0 && r < rn.size()) {
            if (b >= 0 && b < t.size() && av.get(b)) {
                if (debt.get(r) == 0) {
                    // рахуємо, скільки книг уже на руках
                    int cnt = 0;
                    for (int i = 0; i < loans.size(); i++) {
                        if (loans.get(i)[0] == r) {
                            cnt++;
                        }
                    }
                    int max;
                    if (rt.get(r) == 1) {
                        max = 5;
                    } else if (rt.get(r) == 2) {
                        max = 10;
                    } else {
                        max = 2;
                    }
                    if (cnt < max) {
                        av.set(b, false);
                        loans.add(new int[]{r, b});
                        ld.add(d);
                        int days;
                        if (rt.get(r) == 1) {
                            days = 14;
                        } else if (rt.get(r) == 2) {
                            days = 30;
                        } else {
                            days = 7;
                        }
                        System.out.println("Книгу \"" + t.get(b) + "\" видано читачу " + rn.get(r)
                                + ", повернути до " + d.plusDays(days));
                        return "Книгу видано";
                    } else {
                        return "Перевищено ліміт книг для читача";
                    }
                } else {
                    return "Спочатку погасіть заборгованість";
                }
            } else {
                return "Книга недоступна";
            }
        } else {
            return "Читач не зареєстрований";
        }
    }

    // повернення книги
    public String ret(int r, int b, LocalDate d, boolean damaged) {
        int idx = -1;
        for (int i = 0; i < loans.size(); i++) {
            if (loans.get(i)[0] == r && loans.get(i)[1] == b) {
                idx = i;
            }
        }
        if (idx == -1) {
            return "Книга не видавалась цьому читачу";
        }
        if (d.isBefore(ld.get(idx))) {
            return "Дата повернення раніше дати видачі";
        }
        int days;
        if (rt.get(r) == 1) {
            days = 14;
        } else if (rt.get(r) == 2) {
            days = 30;
        } else {
            days = 7;
        }
        long over = ChronoUnit.DAYS.between(ld.get(idx).plusDays(days), d);
        double f = 0;
        if (over > 0) {
            double rate;
            if (rt.get(r) == 1) {
                rate = 2.0;
            } else if (rt.get(r) == 2) {
                rate = 1.0;
            } else {
                rate = 5.0;
            }
            f = over * rate;
            if (f > 100) {
                f = 100;
            }
        }
        if (damaged) {
            f = f + 50;
        }
        debt.set(r, debt.get(r) + f);
        av.set(b, true);
        loans.remove(idx);
        ld.remove(idx);
        if (f == 0) {
            return "Книгу прийнято";
        }
        return "Книгу прийнято, нараховано штраф " + String.format("%.2f", f) + " грн";
    }

    // оплата заборгованості
    public String pay(int r, double s) {
        if (r < 0 || r >= rn.size()) {
            return "Читач не зареєстрований";
        }
        if (s <= 0) {
            return "Сума має бути додатною";
        }
        double rest = debt.get(r) - s;
        if (rest < 0) {
            rest = 0;
        }
        debt.set(r, rest);
        return "Заборгованість: " + String.format("%.2f", rest) + " грн";
    }

    // звіт по читачах
    public void report() {
        for (int i = 0; i < rn.size(); i++) {
            String type;
            if (rt.get(i) == 1) {
                type = "Студент";
            } else if (rt.get(i) == 2) {
                type = "Викладач";
            } else {
                type = "Гість";
            }
            int cnt = 0;
            for (int j = 0; j < loans.size(); j++) {
                if (loans.get(j)[0] == i) {
                    cnt++;
                }
            }
            System.out.println(i + ". " + rn.get(i) + " (" + type + "), книг на руках: " + cnt
                    + ", борг: " + String.format("%.2f", debt.get(i)) + " грн");
        }
    }
}
