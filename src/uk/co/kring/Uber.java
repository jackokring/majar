package uk.co.kring;

/**
 * An abstract class representing a data type with coercion.
 */
public abstract class Uber extends AbstractMultex {

    public Uber(String[] s) {
        super(s);
    }

    /**
     * Obtain an uber of the correct class conversion.
     * @param clazz the kind of class to convert into.
     * @return another uber of the required class or a fail null.
     */
    protected abstract Uber convert(Class<? extends Uber> clazz);

    /**
     * Uber does basis display a different way due to lazy generation cast.
     * @return false.
     */
    protected final boolean listBasis() {
        return false;
    }

    protected abstract Multex optionReplace();

    /**
     * Gets the multex representation if listing of the basis makes no sense without
     * manufacturing it. This maybe because it is inefficient to keep converting it.
     * If you wish to effect a null, just make listBasis return false.
     * @return an effective conversion. Must not return null.
     */
    protected abstract String[] getBasis();

    protected String firstString() {
        if(basis == null) basis = getBasis();
        if(idx >= basis.length) return null;
        return basis[idx];
    }

    protected boolean literalShift(Main m) {
        if(basis == null) basis = getBasis();//template length
        idx++;
        if(idx >= basis.length) {
            m.setError(Main.ERR_LIT, this);
            return true;
        }
        return false;
    }
}
