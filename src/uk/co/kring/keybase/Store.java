package uk.co.kring.keybase;

import uk.co.kring.keybase.stream.BulkStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;

public abstract class Store extends Key {

    UUID instance = UUID.randomUUID();

    abstract void makeReference(Key k);

    abstract boolean isReferenced(Key k);

    abstract String reference(Key k);

    abstract DataInputStream inputStream();

    abstract DataOutputStream outputStream();

    public abstract BulkStream<? extends Key> load();

    public abstract void save(BulkStream<? extends Key> s);
}
