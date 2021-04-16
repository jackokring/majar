package uk.co.kring;

public class Book extends Symbol {

    public Book(String name) {
        super(name, new String[0]);//empty to start
    }

    void run(Main m) {
        m.setCurrent(this);
    }
}
