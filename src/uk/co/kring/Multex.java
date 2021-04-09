package uk.co.kring;

import java.util.List;

public class Multex implements Runnable {

    public void run() {
        //TODO replace this by found
        List<Symbol> s = Main.dict.get(firstString());
        //specifics of multiple dicts?
        Main.ret.push(s.get(0));//Threading ...
    }

    String[] basis;
    int idx = 0;

    public Multex(String[] s) {
        basis = s;
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
