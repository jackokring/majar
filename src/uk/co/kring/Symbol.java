package uk.co.kring;

import static uk.co.kring.Main.nul;

/**
 * A named multex to store in books.
 */
public class Symbol extends AbstractMultex {

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

    protected Multex optionReplace() {
        Symbol s = new Symbol(named, basis);
        s.in = in;
        s.executeIn = executeIn;
        return s;
    }
}
