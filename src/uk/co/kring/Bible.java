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
        //this allows many words with a fixed literal intake to be embedded in escaped literals
        //of an unknown size. If not made into macros the strange condition of "word macro rest"
        //has the interpretation of "word rest" with a macro performed in the escaped literal.
        Prim delay = new Prim() {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(2);
            }
        };
        //TODO main bible hook point

        //1. Word Management
        //==================
        reg(new Macro("list", delay) {
            @Override
            protected void def(Main m) {
                m.list(m.find(m.literal(), true), true);
            }
        });
        reg(new Macro("book", delay) {
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
                m.list(m.context, true);//and print top level
            }
        });
        reg(new Prim("current") {
            @Override
            protected void def(Main m) {
                m.list(m.current, true);
            }
        });
        reg(new Macro("lit", delay) {
            @Override
            protected void def(Main m) {
                m.dat.push(new Multex(m.literal()));
            }
        });
        reg(new Macro("find", delay) {
            @Override
            protected void def(Main m) {
                m.dat.push(m.find(m.literal(), true));//with errors
            }
        });
        reg(new Macro("delay", delay) {
            @Override
            protected void def(Main m) {
                //nop to allow following macro to be postponed
            }
        });
        reg(new Prim("eval") {
            @Override
            protected void def(Main m) {
                m.ret.pop();//pop self
                m.stackForRun(m.dat.pop());
            }
        });

        //2. Variables
        //============
        reg(nul);//a nul value for terminals behaves in strange way
        reg(new Macro("ref", delay) {
            @Override
            protected void def(Main m) {
                reg(new Ref(m.literal(), m.dat.peek()));
            }
        });
        reg(new Macro("space", delay) {
            @Override
            protected void def(Main m) {
                reg(new Space(m.literal()));
            }
        });
        reg(new Macro("time", delay) {
            @Override
            protected void def(Main m) {
                reg(new Time(m.literal(), m.dat.pop()));
            }
        });

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
