package uk.co.kring;

/**
 * Like a book but provides no execution context. It just uses the next word to index and get a value
 * and places it on the data stack.
 */
public class Safe extends Book {

    /**
     * Make an empty safe.
     * @param name the name.
     */
    public Safe(String name) {
        super(name);//empty to start
    }

    protected final boolean run(Main m) {
        Book c = m.switchContext(this);
        Symbol s = m.find(m.literal(), false);
        m.dat.push(s);//blank
        m.switchContext(c);
        m.lastSafe = this;
        return false;
    }
}
