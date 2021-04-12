package uk.co.kring;

public abstract class Prim extends Symbol {

    public void run() {
        def();
    }

    public abstract void def();

    public Prim(String name) {
        super(name, Main.singleton("Prim[" + name + "]"));//get name
    }

    public Prim() {
        this(null);
    }
}
