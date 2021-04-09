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
        if(idx >= basis.length) return null;
        return basis[idx];
    }

    public void shift() {
        idx++;
    }
}
