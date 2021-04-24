package uk.co.kring;

import static uk.co.kring.Main.nul;

/**
 * A named multex to store in books.
 */
public class Symbol extends Multex {

    String named;
    Book in;

    public Symbol(String name, String[] s) {
        super(s);
        if(name == null) {
            Main.getMain().setError(Main.ERR_NUL, nul);
            name = nul.named;//exception?
        }
        named = name;
    }

    public Symbol(Symbol s) {
        this(s.named, s.basis);
        in = s.in;
    }

    protected Multex optionReplace() {
        return new Symbol(this);
    }
}
