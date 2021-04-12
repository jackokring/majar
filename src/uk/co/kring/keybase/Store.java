package uk.co.kring.keybase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class Store extends Key {

    UUID instance = UUID.randomUUID();

    abstract void makeReference(Key k);

    abstract boolean isReferenced(Key k);

    abstract String reference(Key k);

    abstract DataInputStream inputStream();

    abstract DataOutputStream outputStream();

    public abstract Stream<? extends Key> load();

    public abstract void save(Stream<? extends Key> s);
}
