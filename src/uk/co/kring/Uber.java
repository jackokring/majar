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

    /**
     * Obtain an uber of the correct class conversion.
     * @param clazz the kind of class to convert into.
     * @return another uber of the required class or a fail null.
     */
    public abstract Uber convert(Class<? extends Uber> clazz);

    protected boolean listBasis() {
        return false;
    }

    /**
     * Gets the multex representation if listing of the basis makes no sense without
     * manufacturing it. This maybe because it is inefficient to keep converting it.
     * If you wish to effect a null, just make listBasis return false.
     * @return an effective conversion. Must not return null.
     */
    protected abstract String[] getBasis();
}
