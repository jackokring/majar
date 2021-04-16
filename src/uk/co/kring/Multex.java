package uk.co.kring;

/**
 * A class to contain an array of strings with some connection to an execution efficiency concept.
 */
public class Multex {

    protected void run(Main m) {
        Symbol s = m.find(firstString(), executeIn);
        if(s != null) {
            if(!m.runningFast()) {
                m.printSymbolName(s);
                m.profile(s);
            }
            m.execute(new Multex(s), m);//Threading ...
            s.idx++;//Simple profiling
        }
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

    public String firstString() {
        if(ended()) return null;
        return basis[idx];
    }

    public void shift() {
        idx++;
    }

    public boolean ended() {
        return idx >= basis.length;
    }
}
