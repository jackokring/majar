package uk.co.kring.keybase;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * An inputStream of data to process.
 */
public class BaseInputStream extends InputStream {

    /**
     * Prevent direct use. May extend with formatting later.
     * @throws IOException always thrown.
     */
    @Override
    public int read() throws IOException {
        throw new IOException();//no byte access??
    }

    /**
     * Construct an input stream to process a base as characters.
     * @param base the base to use for generation.
     */
    public BaseInputStream(Base base) {

    }

    /**
     * A formatted read.
     * @param by the list of formats.
     * @return formatted string.
     * @throws IOException
     */
    public synchronized String read(List<Format<? extends Key>> by) throws IOException {
        return null;
    }
}
