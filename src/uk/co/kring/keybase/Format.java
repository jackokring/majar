package uk.co.kring.keybase;

/**
 * A class representing a select format on a key.
 * @param <K> the kind of key.
 */
public abstract class Format<K extends Key> {

    /**
     * Create a new format.
     * @param clazz the key class to operate on.
     */
    public Format(Class<K> clazz) {

    }
}
