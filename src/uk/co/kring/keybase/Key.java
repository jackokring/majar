package uk.co.kring.keybase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The abstract data keyed item.
 */
public abstract class Key {

    Key replacement;

    void load(Store store) {
        //TODO
    }

    void save(Store store) throws IOException {
        if(store.isReferenced(this)) {
            store.outputStream().writeUTF(store.reference(this));
        } else {
            store.outputStream().writeUTF(this.getClass().getCanonicalName());
            save(store.outputStream());
            store.makeReference(this);
        }
    }

    /**
     * How to load the class from a stream.
     * @param dis stream to use.
     */
    protected abstract void load(DataInputStream dis);

    /**
     * How to save the class to a stream.
     * @param dos stream to use.
     */
    protected abstract void save(DataOutputStream dos);

    /**
     * Query this for connections to something of a class.
     * @param c the class
     * @return a stream of keys.
     */
    public abstract Base find(Class<? extends Key> c);

    /**
     * Query this for connections to other keys in a stream.
     * @param s the query stream.
     * @return a stream of keys.
     */
    public abstract Base find(Base s);

    /**
     * A common name.
     * @return the name.
     */
    public String called() {//mangler?
        return this.getClass().getName();
    }
}
