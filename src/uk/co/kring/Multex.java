package uk.co.kring;

public class Multex implements Runnable {

    public void run() {

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
