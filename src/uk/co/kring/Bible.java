package uk.co.kring;

import java.util.Stack;

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
        Prim delay = new Prim("delay") {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(2);
            }
        };
        Prim singleOpen = new Macro("begin", null) {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(1);
                m.setMacroEscape(-1, this);//open
            }
        };
        Prim delayOpen = new Macro("def", null) {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(2);
                m.setMacroEscape(-1, this);//open
            }
        };
        Prim singleClose = new Macro("end", null) {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(1);
                m.setMacroEscape(1, this);//close
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
                if(m.dat.peek() == null) return;//the null eval
                Multex x = m.ret.pop();//pop self
                m.stackForRun(m.dat.pop());
                m.ret.push(x);
            }
        });

        //2. Variables
        //============
        reg(nul);//a nul value for terminals behaves in strange way
        reg(new Macro("ref", delay) {
            @Override
            protected void def(Main m) {
                m.reg(new Ref(m.literal(), m.dat.peek()));
            }
        });
        reg(new Macro("space", delay) {
            @Override
            protected void def(Main m) {
                m.reg(new Space(m.literal()));
            }
        });
        reg(new Macro("time", delay) {
            @Override
            protected void def(Main m) {
                m.reg(new Time(m.literal(), m.dat.pop()));
            }
        });
        reg(new Macro("safe", delay) {
            @Override
            protected void def(Main m) {
                Safe s = new Safe(m.literal());
                m.lastSafe = s;
                m.reg(s);
            }
        });
        reg(new Macro("store", delay) {
            @Override
            protected void def(Main m) {
                Book c = m.switchContext(m.lastSafe);
                Multex x = m.dat.pop();
                String name = m.literal();
                Symbol s;
                if(!(x instanceof Symbol)) {
                    s = new Symbol(name, x.basis);
                } else {
                    s = new Ref(name, x);//by object reference
                }
                m.reg(s);
                m.switchContext(c);//restore
            }
        });
        reg(Main.getMain().lastSafe);//and the environmental safe

        //3. Input and Output
        //===================
        reg(new Prim("input") {
            @Override
            protected void def(Main m) {
                m.dat.push(m.readReader(m.getIn(), null));
            }
        });
        reg(new Prim("direct") {
            @Override
            protected void def(Main m) {
                Multex in;
                Stack<Multex> r = m.ret;
                m.ret = new ProtectedStack<>(Main.nul);
                while(true) {
                    if(m.exitLoop) break;
                    in = m.readReader(m.getIn(), null);
                    if(in == null) break;
                    m.execute(in);
                };
                m.exitLoop = false;
                m.ret = r;
            }
        });
        reg(new Prim("exit") {
            @Override
            protected void def(Main m) {
                m.exitLoop = true;
                m.ret.push(nul);//fast stack clear
            }
        });
        reg(new Prim("abort") {
            @Override
            protected void def(Main m) {
                m.userAbort();//the big return to master with error
            }
        });
        reg(new Prim("party") {
            @Override
            protected void def(Main m) {
                m.userExit();
            }
        });
        reg(new Prim("print") {
            @Override
            protected void def(Main m) {
                m.list(m.dat.pop(), false);//print encoded form
            }
        });

        //4. Control Structures
        //=====================
        reg(new Prim("false") {
            @Override
            protected void def(Main m) {
                m.dat.push(null);//push false
            }
        });
        reg(Main.getMain().truth = new Prim("true") {
            @Override
            protected void def(Main m) {
                m.dat.push(m.truth);//push true terminal
            }
        });
        reg(new Prim("not") {
            @Override
            protected void def(Main m) {
                if(m.dat.pop() == null) {
                    m.dat.push(m.truth);
                    return;
                }
                m.dat.push(null);//push false
            }
        });
        reg(new Prim("xor") {
            @Override
            protected void def(Main m) {
                Multex q = m.dat.pop();
                Multex p = m.dat.pop();
                if(q != null && p == null) {
                    m.dat.push(q);
                    return;
                }
                if(q == null && p != null) {
                    m.dat.push(p);
                    return;
                }
                m.dat.push(null);//push false
            }
        });
        reg(new Prim("or") {
            @Override
            protected void def(Main m) {
                Multex q = m.dat.pop();
                Multex p = m.dat.pop();
                if(q != null) {//last is best
                    m.dat.push(q);
                    return;
                }
                m.dat.push(p);//push false
            }
        });
        reg(new Prim("and") {
            @Override
            protected void def(Main m) {
                Multex q = m.dat.pop();
                Multex p = m.dat.pop();
                if(q != null && p != null) {//on p true then q eval
                    m.dat.push(q);
                    return;
                }
                m.dat.push(null);//push false
            }
        });
        reg(new Prim("imp") {
            @Override
            protected void def(Main m) {
                Multex q = m.dat.pop();
                Multex p = m.dat.pop();
                if(q != null && p != null) {//on p then q eval
                    m.dat.push(q);
                    return;
                }
                if(p == null) {//on not p truth
                    m.dat.push(m.truth);
                    return;
                }
                m.dat.push(null);//push false
            }
        });
        reg(new Macro("begin", singleOpen) {
            @Override
            protected void def(Main m) {
                m.dat.push(new Multex(m.multiLiteral(m)));//get a balanced multex
            }
        });
        reg(new Macro("end", singleClose) {
            @Override
            protected void def(Main m) {
                m.checkMacro(this);//real execution checks macro state
            }
        });
        reg(new Macro("def", delayOpen) {
            @Override
            protected void def(Main m) {
                String name = m.literal();
                Symbol s = new Symbol(name, m.multiLiteral(m));
                m.reg(s);
            }
        });
        reg(new Prim("para") {
            @Override
            protected void def(Main m) {
                Multex x = m.ret.pop();
                m.dat.push(new Multex(m.literal()));
                m.ret.push(x);
            }
        });
        reg(new Prim("many") {
            @Override
            protected void def(Main m) {
                Multex x = m.ret.pop();
                m.dat.push(new Multex(m.multiLiteral(m)));//get a balanced multex
                m.ret.push(x);
            }
        });
        reg(new Macro("omit", delay) {
            @Override
            protected void def(Main m) {
                if(m.hadError()) m.literal();//skip it
            }
        });
        reg(new Macro("catch", delay) {
            @Override
            protected void def(Main m) {
                if(!m.hadError()) m.literal();//skip it
            }
        });

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
