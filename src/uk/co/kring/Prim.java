package uk.co.kring;

/**
 * An abstract class representing a primitive Java operation. Primitives can be loaded from the "plug"
 * sub-package automatically by using the simple class name with a lowercase first letter (like a method name).
 */
public abstract class Prim extends UnitSymbol {

    protected final void run(Main m) {
        m.printSymbolized(".....");//TODO
        Book b = m.switchContext(executeIn);
        def(m);
        m.switchContext(b);
    }

    /**
     * Override this method for functionality.
     * @param m a main context of execution including stack and book referencing methods.
     */
    protected abstract void def(Main m);

    public Prim(String name) {
        super(name, (String[])null);//get name
    }

    public Prim() {
        this("");
    }//temp name which gets clobbered as null name not allowed

    /**
     * Defines the simple state of not needing threading.
     * Simple is fine for most plugin loading as each load is an instance. For builtin primitives needing
     * instance per use it requires using the thread primitive class instead of this class.
     * @return true if simple. The default is simple.
     */
    protected boolean simple() {
        return true;
    }

    protected Multex optionReplace() {
        if(simple()) return this;
        try {
            Class<?> clazz = this.getClass();
            Prim instance = (Prim)clazz.newInstance();
            instance.named = named;
            instance.in = in;
            instance.executeIn = executeIn;
            return instance;//yes
        } catch(Exception e) {
            Main.getMain().setError(Main.ERR_THREAD, this);
            return this;
        }
    }
}
