package uk.co.kring;

import java.util.List;

public class Multex implements Runnable {

    public void run() {
        //TODO replace this by found
        List<Symbol> s = Main.dict.get(firstString());
        //not null?
        //specifics of multiple dicts?
        Main.execute(new Multex(s.get(0)));//Threading ...
    }

    String[] basis;
    int idx;

    public Multex(String[] s) {
        idx = 0;
        basis = s;
    }

    public Multex(Multex m) {//new idx
        this(m.basis);
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
