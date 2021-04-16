package uk.co.kring;

/**
 * An abstract class representing a primitive Java operation. Primitives can be loaded from the "plug"
 * sub-package automatically by using the simple class name with a lowercase first letter (like a method name).
 */
public abstract class Prim extends Symbol {

    void run(Main m) {
        def(m);
    }

    /**
     * Override this method for functionality.
     * @param m a main context of execution including stack and book referencing methods.
     */
    protected abstract void def(Main m);

    public Prim(String name) {
        super(name, Main.singleton(("Prim#" + name).intern()));//get name
    }

    public Prim() {
        this(null);
    }
}
