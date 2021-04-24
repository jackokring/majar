package uk.co.kring;

import java.util.Stack;
import static uk.co.kring.Main.nul;

/**
 * A class to represent a task computation parallel.
 */
public class Time extends UnitSymbol {

    Multex ref;

    protected void run(Main m) {
        Thread t = new Thread(() -> {
            Main ml = Main.getMain();
            ml.dat.push(m.dat.pop());//input
            ml.stackForRun(ref);
            ml.execute(null);
        });
        t.start();
        m.dat.push(new Prim(named) {
            @Override
            protected void def(Main m) {
                try {
                    t.join();
                    Main ml = Main.threads.get(t);
                    m.dat.push(ml.dat.pop());//output
                    Main.threads.remove(t);//clean
                } catch(Exception e) {
                    //no worries
                }
            }
        });
    }

    public Time(String name, Multex m) {
        super(name, (String[])null);
        ref = m;
    }
}