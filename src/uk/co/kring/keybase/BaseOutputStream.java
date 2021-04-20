package uk.co.kring.keybase;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output connection to a base.
 */
public class BaseOutputStream extends OutputStream {

    /**
     * Create an output stream on a base.
     * @param base the base to use.
     */
    public BaseOutputStream(Base base) {

    }

    /**
     * Prevent direct use. May extend with parser later.
     * @param i not valid.
     * @throws IOException always thrown.
     */
    @Override
    public void write(int i) throws IOException {
        throw new IOException();//no byte access??
    }

    /**
     * Write an operator to the stream.
     * @param op the operator.
     */
    public synchronized void write(Operator<? extends Key> op) {

    }
}
