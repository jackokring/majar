package uk.co.kring.keybase;

/**
 * A class representing a CRUD on a key.
 * @param <K> the kind of key.
 */
public abstract class Operator<K extends Key> {

    K key;

    /**
     * Create a new operator.
     * @param key the key to operate on.
     */
    public Operator(K key) {

    }
}
