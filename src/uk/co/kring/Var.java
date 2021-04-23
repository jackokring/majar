package uk.co.kring;

/**
 * A class to represent a variable recall mechanism.
 */
public class Var extends UnitSymbol {

    protected void run(Main m) {
        m.dat.push(new Multex(basis));
    }

    public Var(String name, Multex m) {
        super(name, m.basis);
    }

    public Var(String name, String value) {
        super(name, value);//parse
    }

    /**
     * Return the value as a string so that variables can be parameterized.
     * @return the input for of the variable.
     */
    public String firstString() {
        return Main.join(basis);
        //.intern();//return the literal value better to do this on dynamic variable name assign
    }
}
