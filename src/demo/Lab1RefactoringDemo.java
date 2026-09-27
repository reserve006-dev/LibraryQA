package demo;

import legacy.LibraryManager;
import library.LibraryService;
import library.ReaderType;

import java.time.LocalDate;

// Лабораторна робота №1: однаковий сценарій виконується на коді до та після рефакторингу.
// Збіг результатів підтверджує, що зовнішня поведінка програми не змінилася.
public class Lab1RefactoringDemo {

    private static int passed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        LibraryManager oldLib = new LibraryManager();
        LibraryService newLib = new LibraryService();

        String[][] books = {
                {"Кобзар", "Тарас Шевченко", "1840"},
                {"Лісова пісня", "Леся Українка", "1911"},
                {"Тіні забутих предків", "Михайло Коцюбинський", "1911"},
                {"Захар Беркут", "Іван Франко", "1883"}
        };
        for (String[] b : books) {
            oldLib.addBook(b[0], b[1], Integer.parseInt(b[2]));
            newLib.addBook(b[0], b[1], Integer.parseInt(b[2]));
        }
        oldLib.addReader("Олена Коваль", 19, 1);
        newLib.registerReader(ReaderType.STUDENT, "Олена Коваль", 19);
        oldLib.addReader("Петро Іванюк", 45, 2);
        newLib.registerReader(ReaderType.TEACHER, "Петро Іванюк", 45);
        oldLib.addReader("Марія Бондар", 30, 3);
        newLib.registerReader(ReaderType.GUEST, "Марія Бондар", 30);

        LocalDate start = LocalDate.of(2026, 9, 1);

        System.out.println("Порівняння роботи коду до та після рефакторингу");
        System.out.println("-----------------------------------------------------------------");

        check("Видача книги студенту",
                oldLib.issue(0, 0, start),
                newLib.issueBook(0, 0, start).getMessage());
        check("Видача вже виданої книги",
                oldLib.issue(1, 0, start),
                newLib.issueBook(1, 0, start).getMessage());
        check("Видача незареєстрованому читачу",
                oldLib.issue(99, 1, start),
                newLib.issueBook(99, 1, start).getMessage());
        check("Повернення із запізненням 5 днів",
                oldLib.ret(0, 0, start.plusDays(19), false),
                newLib.returnBook(0, 0, start.plusDays(19), false).getMessage());
        check("Видача читачу з боргом",
                oldLib.issue(0, 1, start.plusDays(19)),
                newLib.issueBook(0, 1, start.plusDays(19)).getMessage());
        check("Оплата боргу",
                oldLib.pay(0, 10),
                "Заборгованість: " + String.format("%.2f", newLib.payFine(0, 10)) + " грн");
        check("Видача після оплати боргу",
                oldLib.issue(0, 1, start.plusDays(20)),
                newLib.issueBook(0, 1, start.plusDays(20)).getMessage());
        check("Гість бере 1-шу книгу",
                oldLib.issue(2, 2, start),
                newLib.issueBook(2, 2, start).getMessage());
        check("Гість бере 2-гу книгу",
                oldLib.issue(2, 3, start),
                newLib.issueBook(2, 3, start).getMessage());
        check("Гість бере 3-тю книгу (ліміт 2)",
                oldLib.issue(2, 0, start),
                newLib.issueBook(2, 0, start).getMessage());
        check("Повернення чужої книги",
                oldLib.ret(1, 2, start.plusDays(3), false),
                newLib.returnBook(1, 2, start.plusDays(3), false).getMessage());
        check("Повернення вчасно",
                oldLib.ret(2, 3, start.plusDays(7), false),
                newLib.returnBook(2, 3, start.plusDays(7), false).getMessage());
        check("Повернення пошкодженої книги із запізненням",
                oldLib.ret(2, 2, start.plusDays(47), true),
                newLib.returnBook(2, 2, start.plusDays(47), true).getMessage());

        System.out.println("-----------------------------------------------------------------");
        System.out.println("Збіглося результатів: " + passed + " з " + total);
    }

    private static void check(String step, String before, String after) {
        total++;
        boolean same = before.equals(after);
        if (same) {
            passed++;
        }
        System.out.printf("%-45s | %s%n", step, same ? "ЗБІГ" : "РОЗБІЖНІСТЬ");
        System.out.println("    до:    " + before);
        System.out.println("    після: " + after);
    }
}
