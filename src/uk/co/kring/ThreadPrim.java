package uk.co.kring;

/**
 * A multi-threaded primitive. Has its own instance variables on execution.
 */
public abstract class ThreadPrim extends Prim {

    /**
     * Set not simple.
     * @return false.
     */
    protected boolean simple() {
        return false;
    }
}
