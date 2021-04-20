package uk.co.kring.keybase;

/**
 * An abstract index class. Multi keyed indexes can use indexes as values.
 * @param <K> the keying class.
 * @param <V> the returned value class.
 */
public abstract class Index<K extends Key, V extends Base> extends Base {

}
