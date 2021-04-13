package uk.co.kring.keybase;

import uk.co.kring.keybase.stream.BulkStream;

public abstract class Base extends Key {

    public abstract BulkStream<Datum> asStream();
    //TODO
}
