package uk.co.kring;

import java.util.LinkedList;
import java.util.List;

/**
 * A book class to store symbols in to have a context and multex associations to words.
 */
public class Book extends UnitSymbol {

    List<Symbol> within = new LinkedList<>();//efficiency and ease of delete

    public Book(String name) {
        super(name, null);//empty to start
    }

    protected void run(Main m) {
        Book c = m.context;
        while(c.in != c) c = c.in;
        if(c != m.bible) {
            //placed here things are faster but a not found error replaces
            //other errors and has an increased risk of running a word in a namespace collision
            //a better compromise
            m.setError(Main.ERR_FOR_CON, this);
        } else {
            m.switchContext(this);
        }
        m.ret.pop();
    }

    /**
     * Registration proxy.
     * @param s what to register.
     */
    protected void reg(Symbol s) {
        Main.getMain().reg(s);
    }
}
