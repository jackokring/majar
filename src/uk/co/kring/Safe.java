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

    protected final void run(Main m) {
        m.current = this;
        Symbol s = m.find(m.literal(), false);
        if(s == null) {
            //no value stack balance
            m.dat.push(new Multex((String[])null));//blank
        } else {
            m.dat.push(new Multex(s.basis));//place the recalled value
        }
        m.current = in;
        m.ret.pop();//and leave execution frame
    }
}
