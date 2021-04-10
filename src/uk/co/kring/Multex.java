package uk.co.kring;

import java.util.List;

public class Multex implements Runnable {

    public void run() {
        List<Symbol> s = Main.dict.get(firstString());
        if(s != null) for(Symbol i: s) {
            if(i.in == Main.context) {
                Main.execute(new Multex(i));//Threading ...
                return;
            }
        }
        //TODO replace this by not-found etc

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
