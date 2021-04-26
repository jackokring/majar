package uk.co.kring;

/**
 * A null item class.
 */
public class Nul extends UnitSymbol {

    protected void run(Main m) {
        m.setError(Main.ERR_NUL, this);//exactly
        m.ret.pop();
    }

    public Nul() {
        super("nul", (String[])null);
    }
}
