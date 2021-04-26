package uk.co.kring;

/**
 * A abstract unitary symbol.
 */
public abstract class UnitSymbol extends Symbol {

    public UnitSymbol(String name, String[] s) {
        super(name, s);
    }

    protected Multex optionReplace() {
        return this;//NO
    }

    public boolean shift(Main m) {
        m.ret.pop();
        return true;
    }

    public String firstString() {
        return named;
    }
}
