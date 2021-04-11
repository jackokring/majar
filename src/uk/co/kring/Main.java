package uk.co.kring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.*;

public class Main {

    private static int err, last, first;//primary error code
    static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    static Stack<Multex> ret = new PStack<>();
    static Stack<Multex> dat = new PStack<>();
    static HashMap<String, List<Symbol>> dict =
            new HashMap<>();
    static Book context = new Bible();
    static Book current = context;
    static boolean fast = false;

    //========================================== ENTRY / EXIT

    public static void main(String[] args) {
        try {
            clearErrors();
            intern(args);//first
            reg(current);
            execute(new Multex(args));
        } catch(RuntimeException e) {
            while(!ret.empty()) {
                //trace
                println();
                print(ANSI_RED + ">" + ret.pop().firstString());
            }
        }
        printErrorSummary();
        System.exit(first);//a nice ...
    }

    public static void userAbort() {
        userAbort(true);
    }

    public static void userAbort(boolean a) {
        System.exit(a?1:0);//generate user abort exit code
    }

    //========================================== INTERPRETER

    public static void execute(Multex s) {
        ret.push(s);
        while(!s.ended()) {
            if(s.firstString() == null) return;//no fail null
            if(!fast) {
                print(ANSI_GREEN + s.firstString());
                profile(s.firstString());
            }
            s.run();
            s.shift();
            if(errOver()) break;//prime errors
        }
        ret.pop();
    }

    static void profile(String s) {
        //TODO
    }

    public static void reg(Symbol s) {
        if(s.named == null) {
            setError(ERR_FIND, "No name given");
            return;
        }
        List<Symbol> ls = dict.get(s.named);
        if(ls == null) {
            ls = new LinkedList<>();
        }
        for(Symbol i: ls) {
            if(i.in == current) if(i.in instanceof Bible) {
                setError(ERR_BIBLE, s);
                return;//no can add
            } else {
                ls.remove(i);
            }
            break;
        }
        ls.add(s);//new
        s.in.basis = Arrays.copyOf(s.in.basis, s.in.basis.length + 1);
        s.in.basis[s.in.basis.length - 1] = s.named;
    }

    public static Multex find(String t) {
        List<Symbol> s = dict.get(t);
        Book c;
        if(s != null) {
            for(Symbol i: s) {
                c = context;
                do {
                    if (i.in == c) {
                        return i;
                    }
                    c = c.in;//next higher context
                } while(c != null);
            }
            Main.setError(Main.ERR_CONTEXT, context);
        }
        //class loading bootstrap of Class named as method camelCase
        String p = t.substring(0, 1).toLowerCase(Locale.ROOT) + t.substring(1);//make run method!!
        p = p.intern();//make findable
        String name = Main.class.getPackage().getName() + ".plug." + p;
        try {
            Class<?> clazz = Class.forName(name);
            //Constructor<?> constructor = clazz.getConstructor(String.class);
            Object instance = clazz.newInstance();
            if(instance instanceof Prim) {
                if(!fast) print(ANSI_BLUE + p);
                ((Prim) instance).named = t;//quick hack to put Prim on a default constructor
                ((Prim) instance).in = context;//N.B. important to bring into context to RUN!!
                //as a system definition, it by nature would be later available in the same context
                //current therefore is for user definitions in majar and not Java
                //this has implications for multiple instances
                return (Multex)instance;
            }
        } catch(Exception e) {
            Main.setError(Main.ERR_FIND, ANSI_BLUE + p);
            return null;
        }
        Main.setError(Main.ERR_FIND, t);
        return null;//not found -- can't be
    }

    static final String para = "\\~";//quirk of the shell

    public static Multex readLine(BufferedReader in) {
        try {
            boolean quote = false;
            int j = 0;
            String l = in.readLine();
            l = l.replace("\\\"", para);
            String[] args = l.split(l);
            for(int i = 0; i < args.length; i++) {
                if(args[i].startsWith("\"")) {
                    quote = true;
                    args[i] = args[i].substring(1);//remove quote
                }
                if(!quote) {
                    j++;
                    //args[j] = args[i].trim();
                }
                else {
                    if(args[i].endsWith("\"")) {
                        quote = false;
                        args[i] = args[i].substring(0, args[i].length() - 1);//remove quote
                    }
                    args[j] += " " + args[i];
                    args[i] = null;
                    if(!quote) j = i;//restore parse
                }
            }
            for(int i = 0; i < args.length; i++) {
                if(args[i] != null) args[i] = args[i].replace(para, "\"");//hack!
            }
            if(quote) setError(ERR_QUOTE, args[j]);
            intern(args);//pointers??
            return new Multex(args);
        } catch (Exception e) {
            setError(ERR_IO, System.in);//Input
            return new Multex(new String[0]);//blank
        }
    }

    public static Multex readString(String in) {
        in = in.replace("\n", " ");
        in = in.replace("\t", " ");
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream((in + "\n").getBytes())));
        return readLine(br);
    }

    //================================================== STRING UTIL

    public static void intern(String[] s) {
        for(int i = 0; i < s.length; i++) {
            s[i] = s[i].intern();//pointers??
        }
    }

    public static String dollar(String s) {
        s = s.replace("\\$", para);
        int i;
        while((i = s.indexOf("$")) != -1) {

            String j = topMost(dat, false).replace("$", para);//recursive
            s = s.substring(0, i) + j + s.substring(i + 1);
        }
        return s.replace(para, "$");
    }

    public static String topMost(Stack<Multex> sm, boolean next) {
        Multex m = sm.peek();
        if(next) m.shift();
        while(m.firstString() == null) {
            m.shift();
            if(m.ended()) m = sm.pop();
        }
        String s = m.firstString();
        if(!next) m.shift();
        return s;
    }

    public static String literal() {
        String s = topMost(ret, true);
        if(!fast) {
            print(ANSI_RED + s);
            //profile(s);
        }
        return s;
    }

    public static String parameter(Stack<Multex> sm, boolean next) {
        Multex m = sm.pop();
        String s = topMost(sm, next);
        sm.push(m);
        return s;
    }

    public static void swap(Stack<Multex> sm) {
        Multex m = sm.pop();
        Multex t = sm.pop();
        sm.push(m);
        sm.push(t);
    }

    public static String join(String[] s) {
        StringBuilder t = new StringBuilder();
        boolean f = false;
        for(String i: s) {
            if(i == null) continue;
            if(f) {
                t.append(" ");
            } else {
                f = true;
            }
            if(i.contains(" ")) {
                t.append("\"");
                t.append(i);
                t.append("\"");
            } else {
                t.append(i);
            }
        }
        return t.toString();
        //return String.join(" ", s);
    }

    //========================================== CMD UTIL

    public static void silentExec(String s) {
        try {
            int x = Runtime.getRuntime().exec(s).waitFor();
            if(x != 0) setError(ERR_PROCESS, s);
        } catch(Exception e) {
            setError(ERR_PROCESS, s);
        }
    }

    //================================================== ERRORS

    static final String[] errorFact = {
        "Input",           //0
        "Stack underflow", //1
        "Stack overflow",  //2
        "Closing \"",      //3
        "External process",//4
        "Not found",       //5
        "Protected f'ing bible",//6
        "Raise you an irrefutable",//7
        "Bad context"     //8
    };

    public static final int ERR_IO = 0;
    public static final int ERR_UNDER = 1;
    public static final int ERR_OVER = 2;
    public static final int ERR_QUOTE = 3;
    public static final int ERR_PROCESS = 4;
    public static final int ERR_FIND = 5;
    public static final int ERR_BIBLE = 6;
    //7
    public static final int ERR_CONTEXT = 8;

    static final int[] errorCode = {//by lines of 4
        2, 3, 5, 7,                     //0
        11, 13, 17, 19                  //4
    };

    static final int[] errorComposites = {
        //compositeErrorCode, errorFact : pair per reduction
        17 * 17, 7
    };

    static void clearErrors() {
        err = 1;
        last = -1;
        first = 0;
    }

    public static void setError(int t, Object o) {
        String s;
        boolean combine = false;
        long e = err;
        if(o == null) {
            s = "No further data";
        } else {
            s = classNamed(o);
        }
        if(first < 1) first = t;
        last = t;
        for(int i = 0; i < errorComposites.length; i+=2) {
            if(t == errorComposites[i + 1]) {
                combine = true;
                break;
            }
        }
        if(!combine) System.err.println();//bang tidy!
        errorPlump(ANSI_RED, t, s);
        t = errorCode[t];//map
        mapErrors(e * t);
    }

    public static String classNamed(Object o) {
        if(o instanceof String) return ANSI_RESET + o;
        if(o instanceof Symbol) return ANSI_BLUE + o.getClass().getName() +
                "[" + classNamed(((Symbol)o).named) + "]";
        //TODO
        if(o instanceof Multex) return classNamed(join(((Multex) o).basis));
        return ANSI_PURPLE + o.getClass().getName() + "[" + Integer.toHexString(o.hashCode()) + "]";
    }

    static void mapErrors(long e) {
        for(int i = 0; i < errorComposites.length; i += 2) {
            if(e % errorComposites[i] == 0) {
                e /= errorComposites[i];
                setError(errorComposites[i + 1], Integer.toString(errorComposites[i]));
                //apply the composite and reduce
            }
        }
        if(e > Integer.MAX_VALUE) throw new RuntimeException("MajarInternal");
        err = (int)e;
    }

    static boolean errOver() {
        if(last < 0) return false;
        return ((long)err << 2) > Integer.MAX_VALUE;
    }

    //========================================= PRINTING

    public static void printErrorSummary() {
        if(last != -1) {
            System.err.println();
            errorPlump(ANSI_RED, last, "Error summary follows:");
            String c = ANSI_YELLOW;
            if(errOver()) c = ANSI_RED;//many errors
            else {
                first = err;//return all if no over
                if(first == 1) first = 0;//no error
                //keep first in summary
            }
            for(int i = 0; i < errorFact.length; i++) {
                if(err == 1) break;
                if(err % errorCode[i] == 0) {
                    errorPlump(c, i, null);
                    err /= errorCode[i];
                }
            }
        }
        last = -1;//errors flushed
    }

    public static void printProfile() {
        //TODO
    }

    static final String errorWord = " error.";

    static void errorPlump(String prefix, int code, String s) {
        System.err.println(prefix +
                "[" + errorCode[code] + "] " +
                errorFact[code] + errorWord + ": " + s);
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void print(String s) {
        if(s == null) return;
        System.out.print(s + " " + ANSI_RESET);
    }

    public static void println() {
        System.out.println(ANSI_RESET);
    }
}
