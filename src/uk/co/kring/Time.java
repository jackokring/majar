package uk.co.kring;

import java.util.Stack;
import static uk.co.kring.Main.nul;

/**
 * A class to represent a task computation parallel.
 */
public class Time extends UnitSymbol {

    protected boolean run(Main m) {
        Thread t = new Thread(() -> {
            Main ml = Main.getMain();
            ml.dat.push(m.dat.pop());//input
            ml.execute(new Multex(basis));
        });
        t.start();
        m.dat.push(new UnitSymbol(named, null) {
            @Override
            protected boolean run(Main m) {
                try {
                    t.join();
                    Main ml = Main.threads.get(t);
                    m.dat.push(ml.dat.pop());//output
                    Main.threads.remove(t);//clean
                } catch(Exception e) {
                    //no worries
                }
                return false;
            }
        });
        return false;
    }

    public Time(String name, String[] s) {
        super(name, s);
    }
}