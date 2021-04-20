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
        //should really proxy formatted
    }

    /**
     * Construct an input stream to process a base as characters.
     * @param base the base to use for generation.
     * @param by the list of formats.
     */
    public BaseInputStream(Base base, List<Format<? extends Key>> by) {

    }

    /**
     * A formatted read.
     * @return formatted string.
     * @throws IOException on stream error.
     */
    public synchronized String readByFormat() throws IOException {
        return null;
    }
}
