package uk.co.kring.newra;

import uk.co.kring.Main;
import uk.co.kring.Prim;

/**
 * An abstract class representing an AI primitive. As AI primitives
 * can later be factorized with exceptions due to singular cases the
 * methods supplied allow for this.
 */
public abstract class NewRaNetPrim extends Prim {
    @Override
    protected final void def(Main m) {
        if(hasException(m)) {
            implementation(m);
            return;
        }
        NewRaNetPrim[] f = factorized(m);
        if(f == null) {
            implementation(m);
            return;
        }
        for(NewRaNetPrim t: f) {
            t.def(m);
        }
    }

    /**
     * Find if the exception state exists and return true if so.
     * @param m a main instance.
     * @return is exceptional state.
     */
    protected boolean hasException(Main m) {
        return false;
    }

    /**
     * Return the factorized and simplified equivalent.
     * @param m a main instance.
     * @return the array of sequential compositions in application execution order.
     */
    protected NewRaNetPrim[] factorized(Main m) {
        return null;
    }

    /**
     * The implementation details. By default throws an exception indicating
     * both the lack of an implementation and also respect to possibility.
     * A joke on not yet coded, koded and bode'd.
     * @param m
     */
    protected void implementation(Main m) {
        throw new NotYetBodedException();
    }
}
