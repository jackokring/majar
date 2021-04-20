package uk.co.kring.keybase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;

/**
 * The primary abstract store for data so that persistence is possible.
 */
public abstract class Store extends Key {

    UUID instance = UUID.randomUUID();

    abstract void makeReference(Key k);

    abstract boolean isReferenced(Key k);

    abstract String reference(Key k);

    abstract DataInputStream inputStream();

    abstract DataOutputStream outputStream();

    /**
     * Load the store.
     * @param baseClass the class identifying the base.
     * @return the stream.
     */
    public abstract Base load(Class<Base> baseClass);

    /**
     * Save to the store.
     * @param base the stream.
     */
    public abstract void save(Base base);
}
