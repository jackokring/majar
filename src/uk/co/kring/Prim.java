package uk.co.kring;

public abstract class Prim extends Symbol {

    void run(Main m) {
        def(m);
    }

    protected abstract void def(Main m);

    public Prim(String name) {
        super(name, Main.singleton(("Prim#" + name).intern()));//get name
    }

    public Prim() {
        this(null);
    }
}
