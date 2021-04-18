package uk.co.kring;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The main interpreter class with utilities for use.
 */
public class Main {

    private static final HashMap<Thread, Main> threads = new HashMap<>();

    static synchronized Main getMain() {//multi threading maker
        Main t = threads.get(Thread.currentThread());
        if(t == null) {
            t = newMain();
        }
        return t;
    }

    static synchronized Main newMain() {
        Main t = new Main();
        threads.put(Thread.currentThread(), t);
        return t;
    }

    private int errorExit, last, first;//primary error code

    protected Stack<Multex> ret = new ProtectedStack<>();
    protected Stack<Multex> dat = new ProtectedStack<>();

    protected HashMap<String, List<Symbol>> dict =
            new HashMap<>();
    final Book bible = new Bible();
    protected Book context = bible;
    protected Book current = context;

    private boolean fast = false;
    private boolean html = false;

    private InputStream in = System.in;
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private PrintStream put = out;
    private BufferedReader br;
    private InputStream toClose;

    //========================================== ENTRY / EXIT

    /**
     * The main entry point for the majar interpreter.
     * @param args command arguments.
     */
    public static void main(String[] args) {
        Main m = getMain();
        try {
            if(m.html) {
                m.print("<span class=\"majar\"><span>");
            }
            m.clearErrors();
            intern(args);//first
            m.reg(m.current);
            m.execute(new Multex(args), m);
        } catch(RuntimeException e) {
            if(!m.ret.empty()) {
                m.stackTrace(m.ret);//destructive print
            }
        }
        m.printErrorSummary();
        m.err.flush();
        if(m.html) {
            m.print("</span></span>");
            m.out.flush();
        } else {
            System.exit(m.first);//a nice ... exit on first finished thread?
        }
    }

    /**
     * The run method to interpret a string.
     * @param s the string to run.
     * @return the error code. The setHTML() method must be used to prevent a system exit.
     */
    public static int run(String s) {//per thread running from text
        Main m = newMain();
        main(m.readString(s).basis);
        return m.first;
    }

    //========================================== USER ABORT

    void userAbort() {
        userAbort(false);
    }

    void userAbort(boolean a) {
        print(ANSI_WARN + "User aborted process.");
        println();
        first = a?0:1;//bash polarity
        if (html) {
            throw new RuntimeException("User abort.");
        } else {
            System.exit(first);//generate user abort exit code
        }
    }

    //========================================== INTERPRETER

    boolean runningFast() {
        return fast;
    }

    private Book switchContext(Book b) {
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

    void execute(Multex s, Main m) {
        ret.push(s);
        while(!s.ended()) {
            if(s.firstString() == null) return;//no fail null
            s.run(m);
            s.shift();
            if(errOver()) break;//prime errors
        }
        ret.pop();
    }

    void profile(Symbol s) {
        //TODO
    }

    void reg(Symbol s) {
        reg(s, current);
    }

    void reg(Symbol s, Book current) {
        List<Symbol> ls = unReg(s, current);
        if(ls == null) return;
        s.executeIn = context;//keep context
        s.in.basis = Arrays.copyOf(s.in.basis, s.in.basis.length + 1);
        s.in.basis[s.in.basis.length - 1] = s.named;
        if(s != bible) {
            s.in = current;
        } else {//bible ...
            s.in = null;//or kill context
        }
        if(s instanceof Book) {
            s.executeIn = null;//clear recent cache
        }
        ls.add(s);//new
    }

    List<Symbol> unReg(Symbol s, Book current) {
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

    void deleteBook(Book b) {
        setError(ERR_BOOK, b);
        for(String i: b.basis) {
            unReg(find(i), b);
        }
        if(b.in.executeIn == b) {
            b.in.executeIn = null;
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

    Symbol find(String t) {
        return find(t, true);//default
    }

    Symbol find(String t, Book b, boolean error) {
        if(b == null) b = context;
        Book c = switchContext(b);
        Symbol s = find(t, true);//default
        switchContext(c);//restore
        return s;
    }

    Symbol find(String t, boolean error) {
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
            if(error) setError(Main.ERR_CONTEXT, context);
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
                if(error) setError(Main.ERR_PLUG, instance);//class always report bad Java?
                return null;
            }
        } catch(Exception e) {
            //lazy mode
            if(context.executeIn != null) {//try recent used books
                return find(t, context.executeIn, error);
            } else {
                if (error) setError(Main.ERR_FIND, t);
                return null;
            }
        }
    }

    static final String para = "\u009B";//quirk of the shell unused representation of "\[["
    static final String htmlPara = "\u0085";//technically NEL, but ...

    Multex readReader(InputStream input, String alternate) {
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
            if(br == null) {
                return readString(alternate);
            }
            return readString(br.readLine());
        } catch (Exception e) {
            setError(ERR_IO, br);//Input
            return readString(alternate);
        }
    }

    Multex readString(String s) {
        boolean quote = false;
        int j = 0;
        if(s == null) s = "";
        s = s.replace("\\\"", para);
        if(html) s = s.replace("&", htmlPara);//input render
        s = s.replace("\n", " ");
        s = s.replace("\t", " ");
        String[] args = s.split("");
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
    }

    Multex readInput() {
        return readReader(in, null);
    }

    //================================================== STRING UTIL

    /**
     * Internalize a sString[] so that the == operator works as a fast comparison.
     * @param s array to internalize.
     */
    public static void intern(String[] s) {
        for(int i = 0; i < s.length; i++) {
            s[i] = s[i].intern();//pointers??
        }
    }

    /**
     * Add escapes to a string to be compliant HTML.
     * @param s a string to escape.
     * @return escaped sting.
     */
    public static String escapeHTML(String s) {
        return s.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                "&#" + c + ";" : new String(Character.toChars(c)))
                .collect(Collectors.joining());
    }

    /**
     * Add escapes of quote marks to a string.
     * @param s a string to escape.
     * @return escaped sting.
     */
    public static String escapeQuote(String s) {
        return s.codePoints().mapToObj(c -> c < 128 && "\"'".indexOf(c) != -1 ?
                "\\" + c : new String(Character.toChars(c)))
                .collect(Collectors.joining());
    }

    /**
     * Convert a string to a 1 element string array.
     * @param s string to use.
     * @return one element string array.
     */
    public static String[] singleton(String s) {
        String[] sa = new String[1];
        sa[0] = s.intern();
        return sa;
    }

    String dollar(String s) {
        s = s.replace("\\$", para);
        int i;
        while((i = s.indexOf("$")) != -1) {

            String j = topMost(dat, false).replace("$", para);//recursive
            s = s.substring(0, i) + j + s.substring(i + 1);
        }
        return s.replace(para, "$");
    }

    static String topMost(Stack<Multex> sm, boolean next) {
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

    String literal() {
        String s = topMost(ret, true);
        if(!fast) {
            printSymbolized(s);
        }
        return s;
    }

    static String parameter(Stack<Multex> sm, boolean next) {
        Multex m = sm.pop();
        String s = topMost(sm, next);
        sm.push(m);
        return s;
    }

    static void swap(Stack<Multex> sm) {
        Multex m = sm.pop();
        Multex t = sm.pop();
        sm.push(m);
        sm.push(t);
    }

    /**
     * Join together a string array applying quotes as though the output can be used by the command line.
     * @param s string array.
     * @return joined string.
     */
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

    void silentExec(Multex s) {
        try {
            int x = Runtime.getRuntime().exec(join(s.basis)).waitFor();
            if(x != 0) setError(ERR_PROCESS, s);
        } catch(Exception e) {
            setError(ERR_PROCESS, s);
        }
    }

    //================================================== ERRORS

    /**
     * The error messages of the interpreter.
     */
    public static final String[] errorFact = {
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
        "Overwritten book. All the words in it are now gone",  //13
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
    //16;
    public static final int ERR_CON_BAD = 17;

    /**
     * The error code primes for indexing.
     */
    public static final int[] errorCode = {//by lines of 4
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

    void clearErrors() {
        errorExit = 1;
        last = -1;
        first = 0;
    }

    void setError(int t, Object o) {
        String s;
        long e = errorExit;
        if(first < 1) first = t;
        last = t;
        err.println();//bang tidy!
        errorPlump(ANSI_ERR, t, o);
        t = errorCode[t];//map
        mapErrors(e * t);
    }

    String withinError(Object o) {
        String s;
        if(o instanceof String) {
            s = join(singleton((String)o));
            return html?escapeHTML(s):s;
        }
        s = ((Symbol)o).named;
        if(o instanceof Prim) return ANSI_PRIM + o.getClass().getName() +
                "[" + withinError(s) + ANSI_PRIM + "]";
        if(o instanceof Book) return ANSI_BOOK + o.getClass().getName() +
                "[" + withinError(s) + ANSI_BOOK + "]";
        if(o instanceof Symbol) return ANSI_SYMBOL + o.getClass().getName() +
                "[" + withinError(s) + ANSI_SYMBOL + "]";
        if(o instanceof Multex) return ANSI_MULTEX + withinError(join(((Multex) o).basis));
        return ANSI_CLASS + o.getClass().getName() + "[" +
                Integer.toHexString(o.hashCode()) + "]";
    }

    void mapErrors(long e) {
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

    boolean errOver() {
        if(last < 0) return false;
        return ((long) errorExit << 2) > Integer.MAX_VALUE;
    }

    //========================================= PRINTING

    private void putError(boolean error) {
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

    void printErrorSummary() {
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

    void printProfile() {
        //TODO
    }

    void errorPlump(String prefix, int code, Object o) {
        print(prefix);
        print("[" + errorCode[code] + "]");
        printLiteral(errorFact[code]);
        if(o != null) {
            print(":");
            print(withinError(o));//may contain escape codes
        } else {
            print(".");
        }
        println();
    }

    void stackTrace(Stack<Multex> s) {
        putError(true);
        println();
        while(!s.empty()) {
            //trace
            print(ANSI_ERR + "@ ");
            printLiteral(s.pop().firstString());
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

    public String ANSI_SYMBOL = ANSI_BLUE;
    public String ANSI_PRIM = ANSI_YELLOW;
    public String ANSI_CLASS = ANSI_PURPLE;
    public String ANSI_MULTEX = ANSI_GREEN;
    public String ANSI_BOOK = ANSI_CYAN;
    public String ANSI_LIT = ANSI_RED;
    public String ANSI_ERR = ANSI_RED;
    public String ANSI_WARN = ANSI_YELLOW;

    static final String[] reflect = {
        "SYMBOL",
        "PRIM",
        "CLASS",
        "MULTEX",
        "BOOK",
        "LIT",
        "ERR",
        "WARN"
    };

    private void print(String s) {
        if(s == null) return;
        put.print(s);
    }

    void printSymbolName(Symbol s) {
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

    void printContext() {
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

    void printSymbol(Symbol s) {
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

    void list(Multex m) {
        println();
        if(m instanceof Symbol) {
            printSymbol((Symbol)m);
        } else {
            printSymbolized(join(m.basis));//as multex
        }
    }

    void printSymbolized(String s) {
        if(s == null) return;
        print(ANSI_LIT);
        printLiteral(join(singleton(s)));//Mutex entry form
        print(" ");
    }

    void printLiteral(String s) {
        if(html) {
            print(escapeHTML(s).replace(htmlPara, "&"));//fix up HTML
        } else {
            print(s);
        }
    }

    void printHTML(String s) {
        if(html) {
            print(s.replace(htmlPara, "&"));//fix up HTML
        } else {
            print(s);
        }
    }

    void printTag(String name, String classOpen, Symbol nameValue) {//else close
        print("<");
        if(classOpen == null) print("/");
        printLiteral(name);
        if(classOpen != null) {
            print(" class=\"");
            printLiteral(classOpen);
            print("\"");
        }
        if(nameValue != null) {
            print(" name=\"");
            printLiteral(nameValue.named);
            print("\" ");
            print(" value=\"");
            printLiteral(join(nameValue.basis));
            print("\"");
        }
        print(">");
    }

    void printSpecialTag(String name) {
        print("<");
        printLiteral(name);
        print(" />");
    }

    private void println() {
        if(html) {
            put.print("<br /></span><span>");//quick!!
        } else {
            put.println(ANSI_RESET);
        }
    }

    /**
     * A print utility to unquote unescape and then possibly HTML escape before printing.
     * @param s the string to print.
     */
    public static void exjectPrint(String s) {
        Main m = getMain();
        m.printLiteral(s.replace("\\\"", "\"").
                replace("\\'", "'"));//better store a few more bytes and exject!
    }

    //=========================================== ADAPTION UTILS

    /**
     * Set the input and output streams on the main class before calling the main.
     * @param i input stream.
     * @param o output stream.
     */
    public static void setIO(InputStream i, PrintStream o) {
        setIO(i, o, o);
    }

    /**
     * Set the input and output streams on the main class before calling the main.
     * @param i input stream.
     * @param o output stream.
     * @param e output error stream.
     */
    public static void setIO(InputStream i, PrintStream o, PrintStream e) {
        Main m = getMain();
        m.in = i;
        m.out = o;
        m.err = e;
    }

    static class PipePrintStream extends PrintStream {

        ByteArrayOutputStream s;

        public PipePrintStream(ByteArrayOutputStream b) {
            super(b);
            s = b;
        }
    }

    static class ThreadPipePrintStream extends PrintStream {

        PipedOutputStream s;

        public ThreadPipePrintStream(PipedOutputStream b) {
            super(b);
            s = b;
        }
    }

    /**
     * Used to get a stream to use as output which can then later be connected to an input.
     * @param threaded is the connection between threads?
     * @return the output writer.
     */
    public static PrintStream getPrintStream(boolean threaded) {
        if(threaded) {
            return new ThreadPipePrintStream(new PipedOutputStream());
        }
        return new PipePrintStream(new ByteArrayOutputStream());
    }

    /**
     * Used to get the input stream associated with a gotten writer stream.
     * @param p the writer.
     * @return the input stream.
     * @throws IOException stream error.
     */
    public static InputStream readPrintStream(PrintStream p) throws IOException {
        if(p instanceof ThreadPipePrintStream) {
            return new PipedInputStream(((ThreadPipePrintStream)p).s);
        }
        if(p instanceof PipePrintStream) {
            return new ByteArrayInputStream(((PipePrintStream) p).s.toByteArray());
        }
        throw new IOException("Can't collapse: " + p.toString());
    }

    /**
     * A waiter for returning a print stream continuation.
     */
    public static class Waiter {

        PrintStream s;
        Thread t;

        public Waiter(PrintStream stream, Thread thread) {
            s = stream;
            t = thread;
        }

        public PrintStream getPrintStream() {
            try {
                t.join();
                threads.remove(t);//helps with gc
            } catch(Exception e) {
                //continue anyway
            }
            return s;
        }
    }

    /**
     * A threaded utility to copy an input stream to an output stream.
     * @param i input stream.
     * @param o output stream.
     * @return the output stream waiter to chain into other processes.
     */
    public static Waiter stream(InputStream i, PrintStream o) {
        Thread bg = new Thread(() -> {
            int t = -1;//end?
            try {
                do {
                    while (i.available() > 0) {
                        o.write(t = i.read());
                    }
                    Thread.yield();//pause
                } while (t != -1);
            } catch(IOException e) {
                //end of stream
            }
        });
        bg.start();
        return new Waiter(o, bg);
    }

    /**
     * Sets HTML mode. This uses tags for styling, and also prevents a system exit. This allows the
     * code to be used for websites so as to not exit the web server process.
     */
    public static void setHTML() {
        Main m = getMain();
        m.html = true;
        for(String i: reflect) {
            Class<?> c = Main.class;
            try {
                Field f = c.getField("ANSI_" + i);
                f.set(c, "</span><span class=\"" + i + "\">");
            } catch (Exception e) {
                m.err.println("Can't set color field");
            }
        }
    }

    /**
     * A utility escape quotes in named strings and sanitize numbers. All unnamed strings or numbers
     * are deleted from the parameter map. Useful for handling servlet requests. Strings override numbers.
     * Exact BCD financial numbers can possibly generate some imprecise double precision numbers.
     * @param params the parameter map.
     * @param stringsIn the keys that strings are in.
     * @param numbersIn the keys that numbers are in.
     */
    public static void cleanParameters(Map<String, String[]> params,
                                               String[] stringsIn, String[] numbersIn) {
        HashMap<String, String[]> m = new HashMap<>();
        if(stringsIn != null) for(String i: stringsIn) {
            //must be an allow not a deny policy to catch programmer desires and proof code
            String k = i.intern();
            String[] strings = params.get(i);
            params.remove(i);//got first
            if(strings != null) {
                for(int j = 0; j < strings.length; j++) {
                    strings[j] = escapeQuote(strings[j]).intern();//entry point used later cost analysis
                }
                m.put(k, strings);//interned escaped strings
            }
        }
        if(numbersIn != null) for(String i: numbersIn) {
            //as numbers may not be quoted in concatenations
            //use of a string surround to do the number conversion is language specific
            //better to cast here
            String k = i.intern();
            String[] strings = params.get(i);
            params.remove(i);//got last
            if(strings != null) {
                for(int j = 0; j < strings.length; j++) {
                    if(strings[j].contains(".")) {
                        double num;
                        try {
                            num = Double.parseDouble(strings[j]);
                        } catch (Exception e) {
                            num = Double.NaN;//might get floating point? Financials 2dp?
                            //consider fixed point rounding integers for financials.
                        }
                        strings[j] = Double.toString(num);
                    } else {
                        long num;
                        try {
                            num = Long.parseLong(strings[j]);
                        } catch (Exception e) {
                            num = Long.MIN_VALUE;//best parse compromise
                        }
                        strings[j] = Long.toString(num);
                    }
                }
                m.put(k, strings);//interned (any) numbers are likely to not be symbol names
            }
        }
        params.clear();//empty the unrequested bad
        for(String i: m.keySet()) {
            params.put(i, m.get(i));//and (re)place strings
        }
    }

    /**
     * Make a safe storage of a map in the interpreter.
     * @param safeName name of the safe to make in the context book (not the programmatic current book).
     * @param params the parameter map.
     */
    public static void makeSafe(String safeName, Map<String, String[]> params) {
        Main m = getMain();
        Safe s = new Safe(safeName.intern());//safes do not evaluate or execute anything inside
        m.reg(s, m.context);
        for(String i: params.keySet()) {//serves you right if you did not clean parameters as no intern()
            Symbol key = new Symbol(i, params.get(i));
            m.reg(key, s);//register keys in safe
        }
    }

    /**
     * Make a new threaded process to process an input stream with code and env parameters.
     * @param what the input stream.
     * @param with the code string.
     * @param params the map of string keys to array of strings.
     * @return the input stream to chain into other processes.
     * @throws IOException on a stream error.
     */
    public static InputStream processHTML(InputStream what, String with,
                                          Map<String, String[]> params) throws IOException {
        PrintStream out = getPrintStream(true);
        (new Thread(() -> {
            Main.setIO(what, out);
            Main.setHTML();//as it needs this for no system exit
            Main.makeSafe("env", params);
            Main.run(with);
        })).start();
        return readPrintStream(out);
    }
}
