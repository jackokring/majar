package uk.co.kring;

import java.util.Stack;

/**
 * A class to contain an array of strings with some connection to an execution efficiency concept.
 */
public class Multex {

    /**
     * The default executive run.
     * @param m context.
     */
    protected void run(Main m) {
        idx++;
        if(idx >= basis.length) {
            m.ret.pop();
        }
        if(firstString() == null) {
            m.ret.push(null);//nothing
        }
        Symbol s = m.find(firstString(), executeIn);
        if(s != null) {
            if(!m.runningFast()) {
                m.printSymbolName(s);//ok
                m.profile(s);
            }
            m.stackForRun(s);//Threading ...
            s.idx++;//Simple profiling of things which got cloned if important to clone
        } else {
            m.ret.push(null);//nothing
        }
    }

    /**
     * Returns an effective clone under executive speed conditions. Some classes return singleton instances.
     * This is to allow multi-threading by supplying a new set of instance variables.
     * @return the replacement or this self for no replacement.
     */
    protected Multex optionReplace() {
        Multex m = new Multex(this.basis);
        m.executeIn = executeIn;
        return m;
    }

    String[] basis;
    int idx;
    Book executeIn;

    public Multex(String[] s) {
        idx = -1;
        basis = s;
        //executeIn = Main.getMain().context;
    }

    public Multex(String s) {
        this(Main.getMain().readString(s));
    }

    /**
     * Gets the first executive string available.
     * @return the executive string.
     */
    public String firstString() {
        if(idx >= basis.length) return null;
        return basis[idx];
    }

    /**
     * Shift in a literal context.
     * @param m a context.
     */
    public void literalShift(Main m) {
        idx++;
        if(idx >= basis.length) {
            m.setError(Main.ERR_LIT, this);
        }
    }
}
