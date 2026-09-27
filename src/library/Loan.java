package library;

import java.time.LocalDate;

// Факт видачі книги читачу (виділення класу замість паралельних списків)
public class Loan {

    private final Reader reader;
    private final Book book;
    private final LocalDate issueDate;
    private final LocalDate dueDate;

    public Loan(Reader reader, Book book, LocalDate issueDate) {
        this.reader = reader;
        this.book = book;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(reader.getLoanDays());
    }

    public Reader getReader() {
        return reader;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
