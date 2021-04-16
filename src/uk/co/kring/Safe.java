package uk.co.kring;

/**
 * Like a book but provides no execution context. It just uses the next word to index and get a value
 * and places it on the data stack.
 */
public class Safe extends Book {

    public Safe(String name) {
        super(name);//empty to start
    }

    protected void run(Main m) {
        m.current = this;
        Multex s = m.find(m.literal());
        if(s == null) {
            //no value stack balance
            m.dat.push(new Multex(new String[0]));//blank
        }
        m.dat.push(new Multex(s.basis));//place the recalled value
        m.current = in;
    }
}
