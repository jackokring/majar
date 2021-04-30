package uk.co.kring;

import java.util.Stack;
import static uk.co.kring.Main.nul;

/**
 * A class to represent a task computation parallel.
 */
public class Time extends UnitSymbol {

    protected void run(Main m) {
        Thread t = new Thread(() -> {
            Main ml = Main.getMain();
            ml.dat.push(m.dat.pop());//input
            ml.execute(new Multex(basis));
        });
        t.start();
        m.dat.push(new UnitSymbol("time-future", null) {
            @Override
            protected void run(Main m) {
                try {
                    t.join();
                    Main ml = Main.getMain(t);
                    m.dat.push(ml.dat.pop());//output
                    Main.deleteMain(t);//clean
                } catch(Exception e) {
                    //no worries
                }
            }
        });
        m.ret.pop();
    }

    public Time(String name, String[] s) {
        super(name, s);
    }

    protected boolean listBasis() {
        return true;
    }
}