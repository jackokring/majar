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
        m.switchContext(this);
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
