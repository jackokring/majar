package uk.co.kring;

/**
 * A class to represent a reference.
 */
public class Ref extends UnitSymbol {

    Multex ref;

    protected void run(Main m) {
        m.dat.push(ref);
    }

    public Ref(String name, Multex m) {
        super(name, (String[])null);
        ref = m;
    }
}
