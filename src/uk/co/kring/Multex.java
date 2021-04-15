package uk.co.kring;

public class Multex implements Runnable {

    public void run() {
        Symbol m = Main.find(firstString(), executeIn);
        if(m != null) {
            if(!Main.runningFast()) {
                Main.printSymbolName(m);
                Main.profile(m);
            }
            Main.execute(new Multex(m));//Threading ...
            m.idx++;//Simple profiling
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
