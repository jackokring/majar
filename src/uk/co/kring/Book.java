package uk.co.kring;

public class Book extends Symbol {

    public Book(Book b, String name) {
        super(b, name, new String[0]);//empty to start
    }
}
