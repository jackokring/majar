package uk.co.kring.keybase;

import java.util.stream.Stream;

public abstract class Base extends Key {

    public abstract Stream<Datum> asStream();
    //TODO
}
