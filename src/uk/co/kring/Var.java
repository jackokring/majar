package uk.co.kring;

/**
 * A class to represent a variable recall mechanism.
 */
public class Var extends Symbol {

    protected void run(Main m) {
        m.dat.push(new Multex(basis));
    }

    public Var(String name, Multex m) {
        super(name, m.basis);
    }

    public Var(String name, String value) {
        super(name, value);//parse
    }

    protected Multex optionReplace() {
        return this;//NO
    }

    public void shift(Main m) {
        m.ret.pop();
    }

    /**
     * Return the value as a string so that variables can be parameterized.
     * @return the input for of the variable.
     */
    public String firstString() {
        return Main.join(basis).intern();//return the literal value
    }

    public boolean ended() {
        return true;//never ending story
    }
}
