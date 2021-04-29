package uk.co.kring;

/**
 * An abstract class representing a data type with coercion.
 */
public abstract class Uber extends AbstractMultex {

    public Uber(String[] s) {
        super(s);
    }

    public Uber(String s) {
        super(s);
    }

    protected boolean listBasis() {
        return false;
    }

    protected abstract String[] getBasis();
}
