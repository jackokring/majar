package uk.co.kring;

/**
 * A multi-threaded primitive. Has its own instance variables on execution.
 */
public abstract class ThreadPrim extends Prim {

    /**
     * Set not simple. For builtin primitives not plugin loaded which need
     * instance variables per use. Specifically this forces clone behaviour on use.
     * Plugins can also force this behaviour, but are cloned instances per context load.
     * @return false.
     */
    protected boolean simple() {
        return false;
    }
}
