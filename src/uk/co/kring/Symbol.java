package uk.co.kring;

public class Symbol extends Multex {

    String named;
    Book in;

    public Symbol(String name, String[] s) {
        super(s);
        in = Main.current;
        named = name;
    }
    public Symbol(String name, String source) {
        super(Main.readString(source));
        in = Main.current;
        named = name;
    }

    public Symbol(Multex m) {
        super(m.basis);
        in = Main.current;
        named = "Symbol[" + Integer.toHexString(m.hashCode()) + "]";
    }
}
