package uk.co.kring;

public class Book extends Symbol {

    public Book(String name) {
        super(name, new String[0]);//empty to start
    }

    public void run() {
        Main.current = this;
    }

}
