package uk.co.kring;

import java.util.List;

public class Multex implements Runnable {

    public void run() {
        Multex m = Main.find(firstString());
        if(m != null) {
            Main.execute(new Multex(m));//Threading ...
            m.idx++;//Simple profiling
            return;
        }
        Main.setError(Main.ERR_FIND, firstString());//as null return from find is a feature
    }

    String[] basis;
    int idx;

    public Multex(String[] s) {
        idx = 0;
        basis = s;
    }

    public Multex(Multex m) {//new idx
        this(m.basis);
        idx = 0;//duplicate work? no error
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
