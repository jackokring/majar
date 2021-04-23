package uk.co.kring;

import static uk.co.kring.Main.nul;

/**
 * The basic bible book of irrevocable words built in to the system interpreter.
 */
public class Bible extends Book {

    public Bible() {
        super("bible");//literal already intern
    }

    String[] reserved = {
            //0. Virtual Reserved
            //===================
            "env", "task",
    };

    void fix() {
        Main m = Main.getMain();
        for(String s: reserved) {
            Symbol f = m.find(s, false);
            if(f != null) m.unReg(f, this, true);
        }
    }

    Bible build() {
        //TODO main bible hook point

        //1. Word Management
        //==================
        reg(new Prim("list") {
            @Override
            protected void def(Main m) {
                m.list(m.find(m.literal(), true));
            }
        });
        reg(new Prim("book") {
            @Override
            protected void def(Main m) {
                String name = m.literal();
                if(name == null) {
                    return;
                }
                Book b;
                reg(b = new Book(name));
                m.current = b;//make ready
            }
        });
        reg(new Prim("author") {
            @Override
            protected void def(Main m) {
                m.current = m.context;//author context
            }
        });
        reg(new Prim("context") {
            @Override
            protected void def(Main m) {
                m.printContext();
                m.list(m.context);//and print top level
            }
        });
        reg(new Prim("current") {
            @Override
            protected void def(Main m) {
                m.list(m.current);
            }
        });

        //2. Variables
        //============
        reg(nul);//a nul value for terminals behaves in strange way

        //3. Input and Output
        //===================

        //4. Control Structures
        //=====================

        //5. Numerics
        //===========

        //6. Advanced Data Types
        //======================

        //7. Transcendentals
        //==================

        //8. Useful Composites
        //====================

        return this;//chain fixes
    }
}
