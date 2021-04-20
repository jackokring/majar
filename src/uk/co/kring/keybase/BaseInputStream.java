package uk.co.kring.keybase;

import java.io.IOException;
import java.io.InputStream;

/**
 * An inputStream of data to process.
 */
public class BaseInputStream extends InputStream {

    @Override
    public int read() throws IOException {
        return 0;
    }

    /**
     * Construct an input stream to process a base as characters.
     * @param base the base to use for generation.
     */
    public BaseInputStream(Base base) {

    }
}
