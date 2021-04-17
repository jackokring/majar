package uk.co.kring.keybase;

import uk.co.kring.generic.BulkStream;

public abstract class Base extends Key {

    public Base(BulkStream<? extends Key> storeLoad) {
        //TODO
    }

    public abstract BulkStream<Datum> asStream();
    //TODO
}
