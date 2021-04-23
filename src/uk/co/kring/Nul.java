package uk.co.kring;

/**
 * A null item class.
 */
public class Nul extends UnitSymbol {

    protected void run(Main m) {
        m.setError(Main.ERR_NUL, this);//exactly
    }

    public Nul() {
        super("nul", (String[])null);
    }

    public String firstString() {
        return null;
    }
}
