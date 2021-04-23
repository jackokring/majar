package uk.co.kring;

/**
 * A book class to store symbols in to have a context and multex associations to words.
 */
public class Book extends Symbol {

    public Book(String name) {
        super(name, new String[0]);//empty to start
    }

    protected void run(Main m) {
        m.current = this;
        this.in.executeIn = this;//cache
        m.ret.pop();//and leave execution frame
    }

    protected Multex optionReplace() {
        return this;//NO
    }

    public String firstString() {
        return named;
    }

    public void shift() {
        //nothing
    }

    public boolean ended() {
        return true;//never ending story
    }

    /**
     * Registration proxy.
     * @param s what to register.
     */
    protected void reg(Symbol s) {
        Main.getMain().reg(s);
    }
}
