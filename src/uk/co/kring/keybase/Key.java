package uk.co.kring.keybase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

public abstract class Key {

    Key replacement;

    void load(Store store) {
        //TODO
    }

    void save(Store store) throws IOException {
        if(store.isReferenced(this)) {
            store.outputStream().writeUTF(store.reference(this));
        } else {
            store.outputStream().writeUTF(this.getClass().getName());
            save(store.outputStream());
            store.makeReference(this);
        }
    }

    public abstract void load(DataInputStream dis);

    public abstract void save(DataOutputStream dos);

    public abstract Stream<? extends Key> find(Class<? extends Key> c);

    public abstract Stream<? extends Key> find(Stream<? extends Key> s);

    public Class<? extends Key> called() {//mangler?
        return this.getClass();
    }
}
