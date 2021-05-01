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
            "task",
    };

    protected void run(Main m) {
        m.current = this;
        //this.in.executeIn = this;//cache NO!!
        m.ret.pop();
    }

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
        Prim delay = new Prim("lit-delay") {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(2);
            }
        };
        Prim singleOpen = new Prim("lit-open") {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(1);
                m.setMacroEscape(-1, this);//open
            }
        };
        Prim specialOpen = new Prim("lit-open-nest") {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(1);
                m.setMacroEscape(-1, this);//open
                m.setMacroPickUp(Main.join(m.multiLiteral(m)));
            }
        };
        Prim delayOpen = new Prim("lit-open-delay") {
            @Override
            protected void def(Main m) {
                m.setMacroLiteral(2);
                m.setMacroEscape(-1, this);//open
            }
        };
        Prim singleClose = new Prim("lit-close") {
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
                m.println();//neat
            }
        });
        reg(new Prim("reg") {
            @Override
            protected void def(Main m) {
                Multex x = m.dat.pop();
                if(x instanceof Symbol) {
                    m.reg((Symbol) x);
                    return;
                }
                m.setError(Main.ERR_NAME, x);
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
                String s = m.literal();
                if(s == null) {
                    m.dat.push(null);
                }
                m.dat.push(new Multex(m.readString(s)));
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
                //the null eval contract eval false is pushed to do nothing
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
        reg(new Macro("time", delayOpen) {
            @Override
            protected void def(Main m) {
                String name = m.literal();
                Symbol s = new Time(name, m.multiLiteral(m));
                m.reg(s);
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
                Multex x = m.dat.pop();
                String name = m.literal();
                Symbol s;
                Book c = m.switchContext(m.lastSafe);
                if(!(x instanceof AbstractMultex)) {
                    s = new Symbol(name, x.basis);
                } else {
                    s = new Ref(name, x);//by object reference
                }
                m.reg(s);
                m.switchContext(c);//restore
            }
        });
        reg(Main.getMain().lastSafe);//and the environmental safe
        reg(new Macro("mean", delay) {
            @Override
            protected void def(Main m) {
                Symbol s = m.find(m.literal(), true);
                m.ret.pop();
                if(s.listBasis()) {
                    m.ret.push(new Multex(s.basis));
                } else {
                    m.ret.push(s);//has its own meaning in context
                }
                m.ret.push(this);
            }
        });

        //3. Input and Output
        //===================
        reg(new Prim("input") {
            @Override
            protected void def(Main m) {
                String in = m.readReader(m.getIn());
                if(in == null) {
                    m.setError(Main.ERR_IO, m.getIn());
                    m.dat.push(null);
                    return;
                }
                Multex x = new Multex(m.readString(in));
                m.dat.push(x);
            }
        });
        reg(new Macro("accept", delay) {//like input but accepts the default following literal
            @Override
            protected void def(Main m) {
                String s = m.literal();
                String in = m.readReader(m.getIn());
                if(in == null) {
                    in = s;
                }
                Multex x = new Multex(m.readString(in));
                m.dat.push(x);
            }
        });
        reg(new Prim("direct") {
            @Override
            protected void def(Main m) {
                String in;
                Stack<Multex> r = m.ret;
                while(true) {
                    m.ret = new ProtectedStack<>(Main.nul);
                    if(m.exitLoop) break;
                    m.println();//clean line
                    in = m.readReader(m.getIn());
                    if(in == null) {
                        m.setError(Main.ERR_IO, m.getIn());
                        break;
                    }
                    m.execute(new Multex(m.readString(in)));
                }
                m.exitLoop = false;
                m.ret = r;
            }
        });
        reg(new Prim("exit") {
            @Override
            protected void def(Main m) {
                m.exitLoop = true;
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
        reg(new Prim("html") {
            @Override
            protected void def(Main m) {
                Main.setHTML();//for tests
                m.printMarked("<span class=\"" + m.getName() + "\"><span>");
            }
        });
        reg(new Prim("ansi") {
            @Override
            protected void def(Main m) {
                Main.setANSI();//for tests
                m.printMarked("</span></span>");
            }
        });
        reg(new Prim("show") {//stack show
            @Override
            protected void def(Main m) {
                Stack<Multex> tmp = new ProtectedStack<>(nul);
                while(!m.dat.empty()) {
                    Multex x = m.dat.pop();
                    tmp.push(x);//save
                    m.list(x, true);
                }
                m.println();
                while(!tmp.empty()) m.dat.push(tmp.pop());//restore
            }
        });

        //4. Control Structures
        //=====================
        reg(Main.getMain().truth = new Prim("true") {
            @Override
            protected void def(Main m) {
                m.dat.push(m.truth);//push true terminal
            }
        });
        reg(Main.getMain().falsely = new Prim("false") {
            @Override
            protected void def(Main m) {
                m.dat.push(null);//push false terminal
            }
        });
        reg(new Prim("elucidate") {
            @Override
            protected void def(Main m) {
                if(m.dat.pop() == null) {
                    m.dat.push(m.falsely);
                    return;
                }
                m.dat.push(m.truth);
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
        reg(new Prim("same") {
            @Override
            protected void def(Main m) {
                Multex q = m.dat.pop();
                Multex p = m.dat.pop();
                if(q != null && p == q) {//on p then q eval
                    m.dat.push(q);
                    return;
                }
                if(p == null && q == null) {//on not p truth
                    m.dat.push(m.truth);
                    return;
                }
                m.dat.push(null);//push false
            }
        });
        reg(new Prim("binequal") {
            @Override
            protected void def(Main m) {
                Multex q = m.dat.pop();
                Multex p = m.dat.pop();
                if(q != null && p != null) {//on p then q eval
                    m.dat.push(q);
                    return;
                }
                if(p == null && q == null) {//on not p truth
                    m.dat.push(m.truth);
                    return;
                }
                m.dat.push(null);//push false
            }
        });
        Macro begin;
        reg(begin = new Macro("begin", singleOpen) {
            @Override
            protected void def(Main m) {
                m.dat.push(new Multex(m.multiLiteral(m),
                        m.context));//get a balanced multex
            }
        });
        reg(new Macro("end", singleClose) {
            @Override
            protected void def(Main m) {
                m.setError(Main.ERR_BRACKET, this);
                //real execution checks macro state
            }
        });
        reg(new Macro("nest", specialOpen) {
            @Override
            protected void def(Main m) {
                //behave as a begin, but be conscious it's really about \" elimination
                m.dat.push(new Multex(m.multiLiteral(m), m.context));//get a balanced multex
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
                //undo any literal quotes
                m.dat.push(new Multex(m.readString(m.literal()), m.context));
                m.ret.push(x);
            }
        });
        reg(new Prim("many") {
            @Override
            protected void def(Main m) {
                Multex x = m.ret.pop();
                String y = m.literal();
                Symbol s = m.find(y, false);
                //consume begin token to balance nesting without illogical situation
                if(s != begin) {
                    m.setError(Main.ERR_BEGIN, s != null ? s : y);
                    m.dat.push(null);
                    m.ret.push(x);
                }
                m.dat.push(new Multex(m.multiLiteral(m), m.context));//get a balanced multex
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
        reg(new Prim("ok") {
            @Override
            protected void def(Main m) {
                m.clearErrors();
            }
        });
        reg(new Macro("while", singleOpen) {
            @Override
            protected void def(Main m) {
                String[] x = m.multiLiteral(m);
                if(m.dat.pop() != null) {
                    m.ret.pop();
                    m.ret.push(new UnitSymbol("while-loop", x) {//creates a non literal while iterator
                        @Override
                        protected void run(Main m) {
                            if(m.dat.pop() != null) {
                                m.ret.push(new Multex(x, m.context));//the loop
                                m.ret.push(this);//recursive
                            }
                        }
                    });
                    m.ret.push(new Multex(x, m.context));//the loop
                    m.ret.push(this);//for exit
                }
            }
        });
        reg(new Prim("fast") {
            @Override
            protected void def(Main m) {
                m.fast = true;
            }
        });
        reg(new Prim("slow") {
            @Override
            protected void def(Main m) {
                m.fast = false;
            }
        });
        reg(new Prim("force") {
            @Override
            protected void def(Main m) {
                m.setError(Main.ERR_FORCE, this);//force error to fail
                m.clearErrors();//no error so ok
            }
        });
        reg(new Prim("later") {
            @Override
            protected void def(Main m) {
                m.ret.pop();//self
                Multex y = m.ret.pop();
                Multex z = m.ret.pop();
                m.ret.push(y);//save rest of code to do later after return finished
                m.ret.push(z);
                m.ret.push(this);
            }
        });
        reg(new Prim("continue") {
            @Override
            protected void def(Main m) {
                m.ret.pop();//self
                m.ret.pop();//drop loop
                m.ret.push(this);
            }
        });
        reg(new Prim("break") {
            @Override
            protected void def(Main m) {
                m.ret.pop();//self
                m.ret.pop();//drop loop
                m.ret.pop();//drop iterator
                m.ret.push(this);
            }
        });
        reg(new Prim("after") {
            @Override
            protected void def(Main m) {
                m.ret.pop();//self
                Multex y = m.ret.pop();
                m.ret.push(m.dat.pop());// >R
                m.ret.push(y);
                m.ret.push(this);
            }
        });
        reg(new Prim("outer") {
            @Override
            protected void def(Main m) {
                m.ret.pop();//self
                Multex y = m.ret.pop();
                m.dat.push(m.ret.pop());// R>
                m.ret.push(y);
                m.ret.push(this);
            }
        });
        reg(new Macro("until", singleOpen) {
            @Override
            protected void def(Main m) {
                String[] x = m.multiLiteral(m);
                m.ret.pop();
                m.ret.push(new UnitSymbol("until-loop", x) {//creates a non literal until iterator
                    @Override
                    protected void run(Main m) {
                        Multex t = m.dat.pop();
                        if(t == null) {
                            m.ret.push(new Multex(x, m.context));//the loop
                            m.ret.push(this);
                        } else {
                            m.dat.push(t);//return the ending truth
                            //opposite of while which consumes a truth to start
                        }
                    }
                });
                m.ret.push(new Multex(x, m.context));//the loop
                m.ret.push(this);//for exit
            }
        });
        reg(new Macro("ignore", singleOpen) {
            @Override
            protected void def(Main m) {
                m.multiLiteral(m);//get a balanced multex and ignore it -- comments?
            }
        });
        reg(new Prim("dup") {
            @Override
            protected void def(Main m) {
                Multex x = m.dat.peek();
                m.dat.push(x);
            }
        });
        reg(new Prim("drop") {
            @Override
            protected void def(Main m) {
                m.dat.pop();
            }
        });
        reg(new Prim("over") {
            @Override
            protected void def(Main m) {
                Multex x = m.dat.pop();
                Multex y = m.dat.pop();
                m.dat.push(y);
                m.dat.push(x);
                m.dat.push(y);
            }
        });
        reg(new Prim("swap") {
            @Override
            protected void def(Main m) {
                Multex x = m.dat.pop();
                Multex y = m.dat.pop();
                m.dat.push(x);
                m.dat.push(y);
            }
        });
        reg(new Prim("nip") {
            @Override
            protected void def(Main m) {
                Multex x = m.dat.pop();
                m.dat.pop();
                m.dat.push(x);
            }
        });
        reg(new Prim("tuck") {
            @Override
            protected void def(Main m) {
                Multex x = m.dat.pop();
                Multex y = m.dat.pop();
                m.dat.push(x);
                m.dat.push(y);
                m.dat.push(x);
            }
        });
        reg(new Macro("if", singleOpen) {
            @Override
            protected void def(Main m) {
                String[] x = m.multiLiteral(m);//get a balanced multex
                if(m.dat.pop() != null) {
                    m.ret.pop();
                    m.ret.push(new Multex(x, m.context));
                    m.ret.push(this);
                }
            }
        });
        reg(new Macro("either", singleOpen) {
            @Override
            protected void def(Main m) {
                String[] x = m.multiLiteral(m);//get a balanced multex
                if(m.dat.peek() != null) {
                    m.ret.pop();
                    m.ret.push(new Multex(x, m.context));
                    m.ret.push(this);
                }
            }
        });
        reg(new Macro("else", singleOpen) {
            @Override
            protected void def(Main m) {
                String[] x = m.multiLiteral(m);//get a balanced multex
                if(m.dat.pop() == null) {
                    m.ret.pop();
                    m.ret.push(new Multex(x, m.context));
                    m.ret.push(this);
                }
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
