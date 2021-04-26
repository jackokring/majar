package uk.co.kring;

import java.util.Stack;
import static uk.co.kring.Main.nul;

/**
 * A class to represent a stack computation space.
 */
public class Space extends UnitSymbol {

    Stack<Multex> ref;

    protected void run(Main m) {
        m.dat = ref;//restore
    }

    public Space(String name) {
        super(name, (String[])null);
        Main m = Main.getMain();
        ref = m.dat;
        m.dat = new ProtectedStack<>(nul);//new stack
    }
}