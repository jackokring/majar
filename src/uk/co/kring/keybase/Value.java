package uk.co.kring.keybase;

/**
 * An abstract typed item of type by field.
 * @param <K> the value class.
 */
public abstract class Value<K extends Key> extends Key {

    Field<K> field;
    K key;
}
