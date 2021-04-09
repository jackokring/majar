package uk.co.kring;

public class Symbol extends Multex {

    String named;
    Book in;

    public Symbol(Book b, String name, String[] s) {
        super(s);
        in = b;
        named = name;
    }
}
