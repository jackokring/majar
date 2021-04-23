package uk.co.kring;

/**
 * A macro class which has execution effect when part of a multi-literal.
 * As a single literal it has no macro effect. There is no need for
 * the excesses of word look up for single literals, but in multi-literal
 * sequences immediate macros can be especially useful.
 */
public abstract class Macro extends Prim {

    /**
     * Contains the macro immediate effect
     */
    protected Prim onLiteral;//the macro behaviour

    /**
     * Execute immediate macro primitive.
     * @param m context.
     */
    public final void macroExecute(Main m) {
        if(onLiteral == null) onLiteral = this;//default sensible
        onLiteral.executeIn = executeIn;
        onLiteral.run(m);//run macro in same context
    }

    /**
     * Create a macro with an immediate behaviour as well as being a normal primitive.
     * @param name name to use.
     * @param immediate the immediate macro behaviour.
     */
    public Macro(String name, Prim immediate) {
        super(name);//get name
        onLiteral = immediate;
    }
}
