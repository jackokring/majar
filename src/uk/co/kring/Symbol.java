package uk.co.kring;

public class Symbol extends Multex {

    String named;
    Book in;

    public Symbol(String name, String[] s) {
        super(s);
        named = name;
    }
    public Symbol(String name, String source) {
        super(Main.getMain().readString(source));
        named = name;
    }

    public Symbol(Multex m) {
        super(m.basis);
        named = ("Symbol#" + Integer.toHexString(m.hashCode())).intern();
    }
}
