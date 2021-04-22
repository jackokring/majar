package uk.co.kring;

/**
 * A null item class.
 */
public class Nul extends Symbol {

    /**
     * The bad executive run.
     * @param m context.
     */
    protected void run(Main m) {
        m.setError(Main.ERR_NUL, this);//exactly
        m.ret.pop();
    }

    /**
     * Create a null.
     */
    public Nul() {
        super("nul", (String[])null);
    }

    /**
     * Gets the first executive string available (isn't one).
     * @return the executive string.
     */
    public String firstString() {
        return null;
    }

    /**
     * Shifts to the next executive string (isn't one)..
     */
    public void shift() {

    }

    /**
     * There are no more executive strings.
     * @return end of strings.
     */
    public boolean ended() {
        return true;
    }
}
