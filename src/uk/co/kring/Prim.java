package uk.co.kring;

public abstract class Prim extends Symbol {

    public void run() {
        def();
    }

    public abstract void def();

    public Prim(String name) {
        super(name, ("Prim[" + name + "]").split("\n"));//get name
    }

    public Prim() {
        this(null);
    }
}
