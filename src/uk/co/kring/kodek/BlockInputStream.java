package uk.co.kring.kodek;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * A stream to read blocks from.
 */
public class BlockInputStream extends FilterInputStream {

    protected BlockInputStream(InputStream inputStream) {
        super(inputStream);
    }
}
