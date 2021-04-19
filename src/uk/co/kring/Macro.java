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
    protected Multex onLiteral;//the macro behaviour

    public final void macroExecute(Main m) {
        if(onLiteral == null) onLiteral = this;//default sensible
        m.execute(onLiteral, m);//run the macro
    }
}
