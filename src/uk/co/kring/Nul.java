package uk.co.kring;

/**
 * A null item class.
 */
public class Nul extends UnitSymbol {

    protected boolean run(Main m) {
        m.setError(Main.ERR_NUL, this);//exactly
        return false;
    }

    public Nul() {
        super("nul", (String[])null);
    }
}
