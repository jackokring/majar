package uk.co.kring;

/**
 * A class to represent a variable recall mechanism.
 */
public class Var extends Symbol {

    protected void run(Main m) {
        m.dat.push(new Multex(basis));
        m.ret.pop();//exit exec
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

    public void shift() {
        //nothing
    }

    public String firstString() {
        return Main.join(basis);//return the literal value
    }

    public boolean ended() {
        return true;//never ending story
    }
}
