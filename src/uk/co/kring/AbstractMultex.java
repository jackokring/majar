package uk.co.kring;

/**
 * An abstract base class for abstraction objects using the base multex class.
 */
public abstract class AbstractMultex extends Multex {

    public AbstractMultex(String[] s) {
        super(s);
    }

    protected abstract boolean listBasis();

    protected abstract void run(Main m);

    protected abstract Multex optionReplace();

    /**
     * Provides for running the multex under this class. This is provided as not all subclasses
     * need to override this behaviour. It could be part of the new behaviour.
     * @param m an execution context.
     */
    protected void proxyRun(Main m) {
        super.run(m);//proxy run for super.super
    }
}
