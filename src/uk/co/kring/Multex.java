package uk.co.kring;

/**
 * A class to contain an array of strings with some connection to an execution efficiency concept.
 */
public class Multex {

    /**
     * The default executive run.
     * @param m context.
     */
    protected void run(Main m) {
        if(firstString() == null) return;//no fail null
        Symbol s = m.find(firstString(), executeIn, true);
        if(s != null) {
            if(!m.runningFast()) {
                m.printSymbolName(s);
                m.profile(s);
            }
            m.stackForRun(s);//Threading ...
            s.idx++;//Simple profiling of things which got cloned if important to clone
        }
    }

    /**
     * Returns an effective clone under executive speed conditions. Some classes return singleton instances.
     * This is to allow multi-threading by supplying a new set of instance variables.
     * @return the replacement or this self for no replacement.
     */
    protected Multex optionReplace() {
        return new Multex(this);
    }

    String[] basis;
    int idx;
    Book executeIn;

    public Multex(String[] s) {
        idx = 0;
        basis = s;
    }

    public Multex(Multex m) {//new idx
        this(m.basis);
        idx = 0;//duplicate work? no error
        executeIn = m.executeIn;
    }

    /**
     * Gets the first executive string available.
     * @return the executive string.
     */
    public String firstString() {
        if(ended()) return null;
        return basis[idx];
    }

    /**
     * Shifts to the next executive string.
     */
    public void shift(Main m) {
        idx++;
        if(ended()) {
            m.ret.pop();
        }
    }

    /**
     * There are no more executive strings.
     * @return end of strings.
     */
    public boolean ended() {
        return idx >= basis.length;
    }


}
