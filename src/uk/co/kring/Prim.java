package uk.co.kring;

import java.lang.reflect.Constructor;

/**
 * An abstract class representing a primitive Java operation. Primitives can be loaded from the "plug"
 * sub-package automatically by using the simple class name with a lowercase first letter (like a method name).
 */
public abstract class Prim extends Symbol {

    protected final void run(Main m) {
        def(m);
        m.ret.pop();//and leave execution frame
    }

    /**
     * Override this method for functionality.
     * @param m a main context of execution including stack and book referencing methods.
     */
    protected abstract void def(Main m);

    public Prim(String name) {
        super(name, Main.singleton(name));//get name
    }

    public Prim() {
        this(null);
    }

    /**
     * Defines the simple state of not needing threading.
     * @return true if simple. The default is simple.
     */
    protected boolean simple() {
        return true;
    }

    protected Multex optionReplace() {
        if(simple()) return this;
        try {
            Class<?> clazz = this.getClass();
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object instance = constructor.newInstance(named);
            return (Prim)instance;//yes
        } catch(Exception e) {
            Main.getMain().setError(Main.ERR_THREAD, this);
            return this;
        }
    }

    public void shift() {
        //nothing
    }

    public boolean ended() {
        return false;//never ending story
    }
}
