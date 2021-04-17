package uk.co.kring.generic;

/**
 * An interface to compare things in a radix sort.
 * @param <T> the type generic.
 */
public interface RadixComparable<T> extends Comparable<T> {

    int compareTo(T o, int reduce);//radix reduction index

    int radixSplit();//returns the split (divide by split^reduce)

}
