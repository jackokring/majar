package uk.co.kring;

import java.util.LinkedList;
import java.util.List;

/**
 * A book class to store symbols in to have a context and multex associations to words.
 */
public class Book extends UnitSymbol {

    List<Symbol> within = new LinkedList<>();//efficiency and ease of delete
    Book cache;

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
            //The Parallel Book Hypothesis
            //Within the meaning of other books signalled via name lies a fail, an attempt a not found
            //A re-context and a request for sourcing a meaning.
            //Execution of other thread's words maybe bad, and so is stopped by not letting
            //the book into context.
            //the words go on, and try though, maybe having a better meaning within self.
            //This is not just a speed consideration, it is an optimization of the
            //application of filling in new found words to become the selective source omni.
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
