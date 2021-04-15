package uk.co.kring;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static int errorExit, last, first;//primary error code

    static Stack<Multex> ret = new PStack<>();
    static Stack<Multex> dat = new PStack<>();

    static HashMap<String, List<Symbol>> dict =
            new HashMap<>();
    final static Book bible = new Bible();
    private static Book context = bible;
    private static Book current = context;

    private static boolean fast = false;
    private static boolean html = false;

    private static InputStream in = System.in;
    private static PrintStream out = System.out;
    private static PrintStream err = System.err;
    private static PrintStream put = out;
    private static BufferedReader br;
    private static InputStream toClose;

    //========================================== ENTRY / EXIT

    public static void main(String[] args) {
        try {
            if(html) {
                print("<span>");
            }
            clearErrors();
            intern(args);//first
            reg(current);
            execute(new Multex(args));
        } catch(RuntimeException e) {
            if(!ret.empty()) {
                stackTrace(ret);//destructive print
            }
        }
        printErrorSummary();
        if(html) {
            print("</span>");
        } else {
            System.exit(first);//a nice ...
        }
    }

    public static void userAbort() {
        userAbort(false);
    }

    public static void userAbort(boolean a) {
        print(ANSI_WARN + "User aborted process.");
        println();
        first = a?0:1;//bash polarity
        if (html) {
            throw new RuntimeException("User abort.");
        } else {
            System.exit(first);//generate user abort exit code
        }
    }

    public static int getErrorExit() {
        return first;
    }

    //========================================== INTERPRETER

    public static boolean runningFast() {
        return fast;
    }

    private static Book switchContext(Book b) {
        if(b.in == null) {
            if(!(b instanceof Bible)) {
                setError(ERR_CON_BAD, b);
                b = bible;
            }
        }
        Book c = context;
        context = b;
        return c;
    }

    public static Book getCurrent() {
        return current;
    }

    public static void setCurrent(Book b) {
        current = b;
    }

    public static void execute(Multex s) {
        ret.push(s);
        while(!s.ended()) {
            if(s.firstString() == null) return;//no fail null
            s.run();
            s.shift();
            if(errOver()) break;//prime errors
        }
        ret.pop();
    }

    public static void profile(Symbol s) {
        //TODO
    }

    public static void reg(Symbol s) {
        reg(s, current);
    }

    public static void reg(Symbol s, Book current) {
        List<Symbol> ls = unReg(s, current);
        if(ls == null) return;
        s.executeIn = context;//keep context
        s.in.basis = Arrays.copyOf(s.in.basis, s.in.basis.length + 1);
        s.in.basis[s.in.basis.length - 1] = s.named;
        s.in = current;
        if(s instanceof Book) {
            context = (Book)s;//make context
            s.executeIn = null;//clear recent cache
            s.in.executeIn = (Book)s;//set containing book as last used in
        }
        ls.add(s);//new
    }

    public static List<Symbol> unReg(Symbol s, Book current) {
        if(s.named == null) {
            setError(ERR_NAME, s);
            return null;
        }
        List<Symbol> ls = dict.get(s.named);
        if(ls == null) {
            ls = new LinkedList<>();
        }
        for(Symbol i: ls) {
            if(i.in == current) {
                if(i.in instanceof Bible) {
                    setError(ERR_BIBLE, i);
                    return null;
                } else {
                    if(i instanceof Book) {
                        setError(ERR_BOOK, i);//error
                        deleteBook((Book)i);
                    }
                    ls.remove(i);
                }
            }
            return ls;
        }
        return ls;
    }

    static void deleteBook(Book b) {
        setError(ERR_BOOK, b);
        for(String i: b.basis) {
            unReg(find(i), b);
        }
        b.in = null;//hide context chain for future
        if(b == current) {
            setError(ERR_CUR_DEL, b);
            current = bible;
        }
        if(b == context) {
            setError(ERR_CON_DEL, b);
            context = current;
        }
    }

    public static Symbol find(String t) {
        return find(t, true);//default
    }

    public static Symbol find(String t, Book b) {
        Book c = switchContext(b);
        Symbol s = find(t, true);//default
        switchContext(b);//restore
        return s;
    }

    public static Symbol find(String t, boolean error) {
        List<Symbol> s = dict.get(t);
        Book c;
        if(s != null) {
            c = context;
            do {
                for(Symbol i: s) {
                    if (i.in == c) {
                        return i;
                    }
                }
                c = c.in;//next higher context
            } while(c != null);
            if(error) Main.setError(Main.ERR_CONTEXT, context);
        }
        //class loading bootstrap of Class named as method camelCase
        String p = t.substring(0, 1).toUpperCase(Locale.ROOT) + t.substring(1);//make run method!!
        p = p.intern();//make findable
        String name = Main.class.getPackage().getName() + ".plug." + p;
        try {
            Class<?> clazz = Class.forName(name);
            //Constructor<?> constructor = clazz.getConstructor(String.class);
            Object instance = clazz.newInstance();
            if(instance instanceof Prim) {
                ((Prim) instance).named = t;//quick hack to put Prim on a default constructor
                Book b = current;
                current = context;
                reg((Symbol) instance);
                current = b;//N.B. important to bring into context to RUN!!
                //as a system definition, it by nature would be later available in the same context
                //current therefore is for user definitions in majar and not Java
                //this has implications for multiple instances
                if(!fast) printSymbolName((Symbol)instance);
                return (Symbol)instance;
            } else {
                if(error) Main.setError(Main.ERR_PLUG, instance);//class always report bad Java?
                return null;
            }
        } catch(Exception e) {
            //lazy mode
            if(context.executeIn != null) {//try recent used books
                return find(t, context.executeIn);
            } else {
                if (error) Main.setError(Main.ERR_FIND, t);
                return null;
            }
        }
    }

    static final String para = "\\~";//quirk of the shell

    public static Multex readReader(InputStream input, String alternate) {
        BufferedReader b = null;
        try {
            if(input != toClose && input != null) {
                //yes a different stream
                br.close();
                br = null;
            }
            if(br == null && input != null) {
                br = new BufferedReader(new InputStreamReader(input));//open on demand
                toClose = input;
            }
            b = br;
            if(alternate != null) {
                b = new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream((in + "\n").getBytes())));//alternate text
            }
            boolean quote = false;
            int j = 0;
            String l = b == null? "" : b.readLine();//blanks
            l = l.replace("\\\"", para);
            l = l.replace("\n", " ");
            l = l.replace("\t", " ");
            String[] args = l.split(l);
            for(int i = 0; i < args.length; i++) {
                if(!quote) {
                    if(args[i].startsWith("\"")) {
                        quote = true;
                        args[i] = args[i].substring(1);//remove quote
                    } else {
                        j++;//second or more concat
                    }
                }
                if(quote) {//not quite an else
                    if(args[i].endsWith("\"")) {
                        quote = false;
                        args[i] = args[i].substring(0, args[i].length() - 1);//remove quote
                    }
                }
                if(args[i].contains("\"")) setError(ERR_ESCAPE, args[i].replace(para, "\\\""));
                args[j] += " " + args[i];
                args[i] = null;
                if(!quote) j = i;//restore parse
            }
            for(int i = 0; i < args.length; i++) {
                if(args[i] != null) args[i] = args[i].replace(para, "\"");//hack!
            }
            if(quote) setError(ERR_QUOTE, args[j]);
            intern(args);//pointers??
            return new Multex(args);
        } catch (Exception e) {
            setError(ERR_IO, b);//Input
            return new Multex(new String[0]);//blank
        }
    }

    public static Multex readString(String s) {
        return readReader(null, s);
    }

    public static Multex readInput() {
        return readReader(in, null);
    }

    //================================================== STRING UTIL

    public static void intern(String[] s) {
        for(int i = 0; i < s.length; i++) {
            s[i] = s[i].intern();//pointers??
        }
    }

    public static String escapeHTML(String s) {
        return s.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                "&#" + c + ";" : new String(Character.toChars(c)))
                .collect(Collectors.joining());
    }

    public static String[] singleton(String s) {
        String[] sa = new String[1];
        sa[0] = s.intern();
        return sa;
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
            printSymbolized(s);
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
                t.append(i.replace("\"", "\\\""));//input form
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
        "Input or output problem. Was the process interrupted? OK",           //0
        "Stack underflow. Not enough data was provided to some word", //1
        "Out of memory. Maybe the stack overflowed",  //2
        "Closing \" missing. Check your code",      //3
        "External process error.",//4
        "Word not found. The word is not defined and in a book",       //5
        "Protected f'ing bible. The bible book has reserved words in",//6
        "Raise you an irrefutable. Yes, the bible can't be revoked, but can be expanded",//7
        "Bad context. There is a definition but not in the context chain. Use context",     //8
        "Bad plugin. The Java class to provide a word as a context plugin is not a class extending Prim",  //9
        "No! You can't alter the bible in that way. Consider forking and editing the Java Bible class build method",     //10
        "Quoted string formatted bad. Do not use \" in the middle of words and leave spaces",   //11
        "Symbol with no name. A symbol must have a name to write it into a book",   //12
        "Overwritten book. All the words in it are now gone ",  //13
        "Partial context deleted. Some books in the context chain no longer exist",  //14
        "Current book deleted. Current book set to the bible",  //15
        "Multiple books deleted. A large deletion of books happened",  //16
        "A bad execution context. The book was deleted. 'Tis but a crust",     //17
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
    public static final int ERR_PLUG = 9;
    //10
    public static final int ERR_ESCAPE = 11;
    public static final int ERR_NAME = 12;
    public static final int ERR_BOOK = 13;
    public static final int ERR_CON_DEL = 14;
    public static final int ERR_CUR_DEL = 15;
    public static final int ERR_BOOK_MULTIPLE = 16;
    public static final int ERR_CON_BAD = 17;

    static final int[] errorCode = {//by lines of 4
        2, 3, 5, 7,                     //0
        11, 13, 17, 19,                 //4
        23, 29, 31, 37,                 //8
        41, 43, 47, 53,                 //12
        59, 61, 67, 71,                 //16
        73, 79, 83, 89,                 //20
        97                              //24
    };

    static final int[] errorComposites = {
        //compositeErrorCode, errorFact : pair per reduction
        17 * 17, 7, //raise
        17 * 19, 10, //inform of the possibility of forking
        43 * 43, 16, //multi-book deletion
        43 * 59, 16, //terminal response to multiple books
    };

    public static void clearErrors() {
        errorExit = 1;
        last = -1;
        first = 0;
    }

    public static void setError(int t, Object o) {
        String s;
        long e = errorExit;
        if(first < 1) first = t;
        last = t;
        err.println();//bang tidy!
        errorPlump(ANSI_ERR, t, o);
        t = errorCode[t];//map
        mapErrors(e * t);
    }

    public static String withinError(Object o) {
        if(o instanceof String) return html?escapeHTML((String)o):(String)o;
        if(o instanceof Prim) return ANSI_PRIM + o.getClass().getName() +
                "[" + withinError(((Symbol)o).named) + ANSI_PRIM + "]";
        if(o instanceof Book) return ANSI_BOOK + o.getClass().getName() +
                "[" + withinError(((Symbol)o).named) + ANSI_BOOK + "]";
        if(o instanceof Symbol) return ANSI_SYMBOL + o.getClass().getName() +
                "[" + withinError(((Symbol)o).named) + ANSI_SYMBOL + "]";
        if(o instanceof Multex) return ANSI_MULTEX + withinError(join(((Multex) o).basis));
        return ANSI_CLASS + o.getClass().getName() + "[" +
                Integer.toHexString(o.hashCode()) + "]";
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
        errorExit = (int)e;
    }

    static boolean errOver() {
        if(last < 0) return false;
        return ((long) errorExit << 2) > Integer.MAX_VALUE;
    }

    //========================================= PRINTING

    private static void putError(boolean error) {
        if(error) {
            put = err;
            if(out != err) {
                print("<span>");
            }
        } else {
            if(out != err) {
                print("</span>");
            }
            put = out;
        }
    }

    public static void printErrorSummary() {
        if(last != -1) {
            putError(true);
            println();
            errorPlump(ANSI_ERR, last, "Error summary follows:");
            String c = ANSI_WARN;
            if(errOver()) c = ANSI_ERR;//many errors
            else {
                first = errorExit;//return all if no over
                if(first == 1) first = 0;//no error
                //keep first in summary
            }
            for(int i = 0; i < errorFact.length; i++) {
                if(errorExit == 1) break;
                if(errorExit % errorCode[i] == 0) {
                    errorPlump(c, i, null);
                    errorExit /= errorCode[i];
                }
            }
            putError(false);
        }
        last = -1;//errors flushed
    }

    public static void printProfile() {
        //TODO
    }

    static void errorPlump(String prefix, int code, Object o) {
        print(prefix);
        print("[" + errorCode[code] + "]");
        printLiteral(errorFact[code]);
        if(o != null) {
            print(":");
            print(withinError(o));
        } else {
            print(".");
        }
        println();
    }

    public static void stackTrace(Stack<Multex> s) {
        putError(true);
        println();
        while(!s.empty()) {
            //trace
            print(ANSI_ERR + "@ " + s.pop().firstString());
            println();
        }
        println();
        putError(false);
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

    public static String ANSI_SYMBOL = ANSI_BLUE;
    public static String ANSI_PRIM = ANSI_YELLOW;
    public static String ANSI_CLASS = ANSI_PURPLE;
    public static String ANSI_MULTEX = ANSI_GREEN;
    public static String ANSI_BOOK = ANSI_CYAN;
    public static String ANSI_LIT = ANSI_RED;
    public static String ANSI_ERR = ANSI_RED;
    public static String ANSI_WARN = ANSI_YELLOW;

    static String[] reflect = {
        "SYMBOL",
        "PRIM",
        "CLASS",
        "MULTEX",
        "BOOK",
        "LIT",
        "ERR",
        "WARN"
    };

    private static void print(String s) {
        if(s == null) return;
        put.print(s);
    }

    public static void printSymbolName(Symbol s) {
        if(s == null) return;
        String c = ANSI_SYMBOL;
        if(s instanceof Prim) c = ANSI_PRIM;
        if(s instanceof Book) c = ANSI_BOOK;
        if(s.named != null) {
            print(c);
            printLiteral(s.named);
            print(" ");
        }
    }

    public static void printContext() {
        print("[");
        printSymbolName(current);
        print("] ");
        Book c = context;
        do {
            printSymbolName(c);
            c = c.in;
        } while(c.in != null);
        print("[");
        c = context;
        while(c.executeIn != null) {
            c = c.executeIn;
            printSymbolName(c);
        }
        print("]");
        println();
    }

    public static void printSymbol(Symbol s) {
        if(s == null) return;
        if(s instanceof Prim) printSymbolName(s);
        Book c = context;
        if(s instanceof Book) {
            context = (Book)s;//set self to view
        }
        for(String i: s.basis) {
            Symbol x = find(i, false);
            if(x != null) {
                printSymbolName(x);
            } else {
                printSymbolized(i);//not found in context
            }
        }
        context = c;
        print(" ");
    }

    public static void list(Multex m) {
        println();
        if(m instanceof Symbol) {
            printSymbol((Symbol)m);
        } else {
            printSymbolized(join(m.basis));//as multex
        }
    }

    public static void printSymbolized(String s) {
        if(s == null) return;
        print(ANSI_LIT);
        printLiteral(join(singleton(s)));//Mutex entry form
        print(" ");
    }

    public static void printLiteral(String s) {
        if(html) {
            print(escapeHTML(s));
        } else {
            print(s);
        }
    }

    private static void println() {
        if(html) {
            put.print("<br /></span><span>");//quick!!
        } else {
            put.println(ANSI_RESET);
        }
    }

    //=========================================== ADAPTION UTILS

    public static Class<Main> setIO(InputStream i, PrintStream o) {
        return setIO(i, o, o);
    }

    public static Class<Main> setIO(InputStream i, PrintStream o, PrintStream e) {
        in = i;
        out = o;
        err = e;
        return Main.class;
    }

    public static Class<Main> setHTML() {
        html = true;
        for(String i: reflect) {
            Class<?> c = Main.class;
            try {
                Field f = c.getField("ANSI_" + i);
                f.set(c, "</span><span class=\"" + i + "\">");
            } catch (Exception e) {
                err.println("Can't set color field");
            }
        }
        return Main.class;
    }
}
