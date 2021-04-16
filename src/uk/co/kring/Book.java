package uk.co.kring;

/**
 * A book class to store symbols in to have a context and multex associations to words.
 */
public class Book extends Symbol {

    public Book(String name) {
        super(name, new String[0]);//empty to start
    }

    protected void run(Main m) {
        m.current = this;
        this.in.executeIn = this;//cache
    }
}
