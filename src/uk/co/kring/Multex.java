package uk.co.kring;

import java.util.List;

public class Multex implements Runnable {

    public void run() {
        List<Symbol> s = Main.dict.get(firstString());
        Book c;
        if(s != null) {
            for(Symbol i: s) {
                c = Main.context;
                do {
                    if (i.in == c) {
                        Main.execute(new Multex(i));//Threading ...
                        return;
                    }
                    c = c.in;//next higher context
                } while(c != null);
            }
            Main.setError(Main.ERR_CONTEXT, Main.context);
        }
        //TODO replace this by classLoader

        Main.setError(Main.ERR_FIND, firstString());
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
