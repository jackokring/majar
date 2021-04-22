package uk.co.kring.kodek;

import java.io.FilterOutputStream;
import java.io.OutputStream;
/**
 * A steam to write output written blocks.
 */
public class BlockOutputStream extends FilterOutputStream {

    public BlockOutputStream(OutputStream outputStream) {
        super(outputStream);
    }
}
