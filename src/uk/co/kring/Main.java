package uk.co.kring;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The main interpreter class with utilities for use.
 */
public class Main {

    protected static final HashMap<Thread, Main> threads = new HashMap<>();

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

    private Thread flusher;//flusher counter

    private int errorExit, last, first;//primary error code
    public static final Symbol nul = new Nul();

    protected Stack<Multex> ret = new ProtectedStack<>(nul);
    protected Stack<Multex> dat = new ProtectedStack<>(nul);

    protected HashMap<String, List<Symbol>> dict =
            new HashMap<>();
    private final Book bible = new Bible();
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

    protected Stack<Frame> macroEscape = new ProtectedStack<>(null);
    protected boolean chaining = false;

    private String givenName = "majar";

    //========================================== ENTRY / EXIT

    /**
     * The main entry point for the majar interpreter.
     * @param args command arguments.
     */
    public static void main(String[] args) {
        Main m = getMain();
        intern(args);//first
        if(args.length > 0) m.givenName = args[0];
        try {
            if(m.html) {
                m.print("<span class=\"" + m.givenName + "\"><span>");
            }
            if(m.chaining) {
                m.chaining = false;
            } else {
                m.clearErrors();
                m.reg(m.bible);
                ((Bible) m.bible).build().fix();
            }
            m.startFlusher();
            m.execute(new Multex(args));
        } catch(Exception e) {
            m.putError(false);
            if(!m.ret.empty()) {
                m.stackTrace(m.ret);//destructive print
            }
        }
        m.exitFlusher();
        m.printErrorSummary();
        if(m.html) {
            m.print("</span></span>");
            synchronized(m) {
                m.out.flush();
            }
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
        int first = m.first;
        if(!m.chaining) threads.remove(Thread.currentThread());//possible saving but sequential reinitialization
        return first;
    }

    //========================================== USER ABORT

    void userAbort() {
        userAbort(true);
    }

    void userExit() {
        userAbort(false);
    }

    private void userAbort(boolean a) {
        print(ANSI_WARN + errorFact.getString("abort"));
        println();
        first = a?1:0;//bash polarity
        if (html) {
            throw new RuntimeException();
        } else {
            System.exit(first);//generate user abort exit code
        }
    }

    //========================================== INTERPRETER

    boolean runningFast() {
        return fast;
    }

    Book switchContext(Book b) {
        if(b == null) return context;
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

    void execute(Multex s) {
        if(s == null) return;
        stackForRun(s);
        while(!ret.empty()) {
            runNext();
            if(errOver()) break;//prime errors
        }
    }

    void stackForRun(Multex s) {
        s = s.optionReplace();//new index required?
        ret.push(s);//place on stack
    }

    void runNext() {//fetch and execute
        Multex m = ret.peek();
        if(m == null) return;//end of code
        m.run(this);
        m.shift(this);//post fetch
    }

    void profile(Symbol s) {
        //TODO
    }

    void reg(Symbol s) {
        reg(s, current);
    }

    void reg(Symbol s, Book current) {
        if(s == null) return;
        if(s == nul) {
            setError(ERR_NUL, nul);
        }
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
        return unReg(s, current,false);
    }

    List<Symbol> unReg(Symbol s, Book current, boolean reserved) {
        if(s == null) return null;
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
                if(i.in instanceof Bible && !reserved) {
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
            unReg(find(i, false), b);
        }
        if(b.in.executeIn == b) {
            b.in.executeIn = null;//clear lazy eval
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

    Symbol find(String t, Book b, boolean error) {
        if(b == null) b = context;
        Book c = switchContext(b);
        Symbol s = find(t, true);//default
        switchContext(c);//restore
        return s;
    }

    Symbol find(String t, boolean error) {
        if(t == null) {
            return null;
        }
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
        if(error) {//if finding for errors then try class any lazy context too
            //class loading bootstrap of Class named as method camelCase
            String p = Character.toUpperCase(t.charAt(0)) + t.substring(1);//make run method!!
            p = p.intern();//make findable
            String name = Main.class.getPackage().getName() + ".plug." + p;
            try {
                Class<?> clazz = Class.forName(name);
                //Constructor<?> constructor = clazz.getConstructor(String.class);
                Object instance = clazz.newInstance();
                if (instance instanceof Prim) {
                    ((Prim) instance).named = t;//quick hack to put Prim on a default constructor
                    Book b = current;
                    current = context;
                    reg((Symbol) instance);
                    current = b;//N.B. important to bring into context to RUN!!
                    //as a system definition, it by nature would be later available in the same context
                    //current therefore is for user definitions in majar and not Java
                    //this has implications for multiple instances
                    if (!fast) printSymbolName((Symbol) instance);
                    return (Symbol) instance;
                } else {
                    setError(Main.ERR_PLUG, instance);//class always report bad Java?
                    return null;
                }
            } catch (Exception e) {
                //lazy mode
                if (context.executeIn != null) {//try recent used books
                    return find(t, context.executeIn, error);
                } else {
                    setError(Main.ERR_FIND, t);
                    return null;
                }
            }
        } else {
            setError(Main.ERR_FIND, t);
            return null;
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
        int j = -1;
        if(s == null) return new Multex((String[])null);//blank
        s = s.replace("\\\"", para);
        if(html) s = s.replace("&", htmlPara);//input render
        s = s.replace("\n", " ");
        s = s.replace("\t", " ");
        String[] args = s.split(" ");
        for(int i = 0; i < args.length; i++) {
            if(!quote) {
                j++;//step
                if(args[i].startsWith("\"")) {
                    quote = true;
                    args[i] = args[i].substring(1);//remove quote
                }
            }
            if(quote) {//not quite an else
                //no step
                if(args[i].endsWith("\"")) {
                    quote = false;
                    args[i] = args[i].substring(0, args[i].length() - 1);//remove quote
                }
            }
            if(args[i].contains("\"")) setError(ERR_ESCAPE, args[i].replace(para, "\\\""));
            if(j != i) {
                args[j] += " " + args[i];//add
                args[i] = null;
            }
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
        if(s == null) return;
        for(int i = 0; i < s.length; i++) {
            if(s[i] == null) continue;
            s[i] = s[i].intern();//pointers??
        }
    }

    /**
     * Add escapes to a string to be compliant HTML.
     * @param s a string to escape.
     * @return escaped sting.
     */
    public static String escapeHTML(String s) {//TODO maybe no intern needed
        if(s == null) return null;
        return s.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                "&#" + c + ";" : new String(Character.toChars(c)))
                .collect(Collectors.joining());
    }

    /**
     * Add escapes of quote marks to a string.
     * @param s a string to escape.
     * @return escaped sting.
     */
    public static String escapeQuote(String s) {//TODO maybe no intern needed
        if(s == null) return null;
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
        sa[0] = s;
        return sa;
    }

    String dollar(String s) {//TODO maybe no intern needed
        if(s == null) return null;
        s = s.replace("\\$", para);
        int i;
        while((i = s.indexOf("$")) != -1) {
            String j = topMost(dat).replace("$", para);//recursive
            s = s.substring(0, i) + j + s.substring(i + 1);
        }
        return s.replace(para, "$");
    }

    String topMost(Stack<Multex> sm) {//null at end
        Multex m = sm.peek();
        while(m == null || m.firstString() == null) {
            if(m == null) {
                sm.pop();//pop null
                m = sm.peek();
            } else {
                if (m == nul) {
                    Main.getMain().setError(ERR_NUL, m);
                    return null;
                }
                if (m.shift(this)) {
                    m = sm.peek();
                }
            }
        }
        String s = m.firstString();
        return s;
    }

    String literal() {
        Multex m = ret.pop();//executive context
        if(macroEscape.peek().shiftSkip) ret.peek().shift(this);//move onto literals
        String s = topMost(ret);//obtain a literal
        //after word execution the final shift is done by runNext()
        macroEscape.peek().shiftSkip = false;//cancel literal macro for reabsorption
        if(!fast) {
            printSymbolized(s);
        }
        ret.push(m);//restore
        return s;
    }

    void setChaining() {
        chaining = true;
    }

    //only call this from within a macro
    void setMacroEscape(int escape, Macro m) {
        if(macroEscape.empty()) {
            setError(ERR_BRACKET, m);
        } else {
            Frame f = macroEscape.peek();
            f.open -= escape;
            if(f.open < 0) {
                setError(ERR_BRACKET, m);
            }
        }
    }

    void setMacroLiteral(int forLength) {
        macroEscape.peek().shiftSkip = true;//reinterpret as literal
        macroEscape.peek().cancelFor = forLength;//macro and following
    }

    boolean skipOne() {//allows "lit delay macro" => return "delay" as "lit macro" in multi-literal
        return macroEscape.peek().cancelFor-- > 0;
    }

    static class Frame {
        boolean shiftSkip = false;
        int cancelFor = 0;
        int open = 1;
    }

    String[] multiLiteral(Main m) {
        LinkedList<String> ls = new LinkedList<>();
        macroEscape.push(new Frame());
        do {
            String s = literal();
            if(s == null) break;//end literal stream
            Symbol f = find(s, false);//not an error if it's not
            if(!skipOne() && f instanceof Macro) {
                if(!fast) {
                    printSymbolName(f);
                }
                ((Macro) f).macroExecute(m);//the macro must potentially set macro escape
            } else {
                ls.addLast(s);
            }
        } while(macroEscape.peek().open > 0);
        macroEscape.pop();
        return fromList(ls);
    }

    String[] fromList(List<String> ls) {
        Object[] o = ls.toArray();
        String[] s = new String[o.length];
        for(int i = 0; i < o.length; i++) {
            s[i] = (String)o[i];
        }
        return s;
    }

    /**
     * Join together a string array applying quotes as though the output can be used by the command line.
     * @param s string array.
     * @return joined string.
     */
    public static String join(String[] s) {
        if(s == null) return null;
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

    InputStream getIn() {
        return in;
    }

    //================================================== ERRORS

    static ResourceBundle errorFact =
            ResourceBundle.getBundle(Main.class.getPackage().getName()
                    + ".lang.Errors", Locale.getDefault());

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
    public static final int ERR_THREAD = 18;
    public static final int ERR_BRACKET = 19;
    public static final int ERR_NUL = 20;
    //21

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
        73 * 73, 21,
        79 * 73, 21,    //null terminal
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
        errorPlump(ANSI_ERR, t, o, true);//on a new line
        t = errorCode[t];//map
        mapErrors(e * t);
    }

    void mapErrors(long e) {
        for(int i = 0; i < errorComposites.length; i += 2) {
            if(e % errorComposites[i] == 0) {
                e /= errorComposites[i];
                setError(errorComposites[i + 1], Integer.toString(errorComposites[i]));
                //apply the composite and reduce
            }
        }
        if(e > Integer.MAX_VALUE) throw new RuntimeException();//now baulk
        errorExit = (int)e;
    }

    boolean errOver() {
        if(last < 0) return false;
        return ((long) errorExit << 2) > Integer.MAX_VALUE;
    }

    //========================================= PRINTING ERROR

    private synchronized void putError(boolean error) {
        put.flush();
        if(error) {
            put = err;
            if(out != err && html) {
                print("<span class=\"" + givenName + "\"><span>");
            }
        } else {
            if(out != err && html) {
                print("</span></span>");
            }
            put = out;
        }
    }

    void printErrorSummary() {
        if(last != -1) {
            putError(true);
            errorPlump(ANSI_ERR, last, errorFact.getString("summary"), true);//on newline
            String c = ANSI_WARN;
            if(errOver()) c = ANSI_ERR;//many errors
            else {
                first = errorExit;//return all if no over
                if(first == 1) first = 0;//no error
                //keep first in summary
            }
            for(int i = 0; i < errorFact.keySet().size(); i++) {
                if(errorExit == 1) break;
                if(errorExit % errorCode[i] == 0) {
                    errorPlump(c, i, null, false);//already newline
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

    void errorPlump(String prefix, int code, Object o, boolean newline) {
        if(newline) println();
        print(prefix);
        print("[" + errorCode[code] + "]");
        printLiteral(errorFact.getString(String.valueOf(code)));//indexed by code
        if(o != null) {
            print(":");
            if(o instanceof Multex) {
                if(o instanceof Symbol) {
                    printSymbolName((Symbol) o);
                } else {
                    list((Multex)o, false);
                }
            } else {
                printColor(o);
                print(o.getClass().getCanonicalName());                ;
            }
        }
        print(prefix);
        print(".");
        println();//final align
    }

    void stackTrace(Stack<Multex> s) {
        putError(true);
        while(!s.empty()) {
            //trace
            Multex m = s.pop();
            println();
            if(m != null) {
                print(ANSI_ERR + "@ ");
                if(m instanceof Symbol) {
                    printSymbolName((Symbol)m);
                } else {
                    list(m, false);
                }
            } else {
                print(ANSI_ERR + "@@");//a void on the stack
            }
        }
        println();
        putError(false);
    }

    //======================================== PRINTING

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";
    public static final String ANSI_UNDER = "\u001B[4m";

    public String ANSI_Object = ANSI_RED;
    public String ANSI_ERR = ANSI_RED + ANSI_BOLD;
    public String ANSI_String = ANSI_RED + ANSI_ITALIC;

    public String ANSI_Symbol = ANSI_GREEN;
    public String ANSI_Space = ANSI_GREEN + ANSI_BOLD;
    public String ANSI_Ref = ANSI_GREEN + ANSI_ITALIC;

    public String ANSI_NewRaNetPrim = ANSI_BLUE;
    public String ANSI_Time = ANSI_BLUE + ANSI_BOLD;
    public String ANSI_Nul = ANSI_BLUE + ANSI_ITALIC;

    public String ANSI_Prim = ANSI_YELLOW;
    public String ANSI_WARN = ANSI_YELLOW + ANSI_BOLD;
    public String ANSI_Macro = ANSI_YELLOW + ANSI_ITALIC;

    public String ANSI_A = ANSI_PURPLE;
    public String ANSI_B = ANSI_PURPLE + ANSI_BOLD;
    public String ANSI_C = ANSI_PURPLE + ANSI_ITALIC;

    public String ANSI_Book = ANSI_CYAN;
    public String ANSI_Bible = ANSI_CYAN + ANSI_BOLD;
    public String ANSI_Safe = ANSI_CYAN + ANSI_ITALIC;

    static final String[] reflect = {
        "Object", "ERR", "String",
        "Symbol", "Space", "Ref",
        "NewRaNetPrim", "Time", "Nul",
        "Prim", "WARN", "Macro",
        "", "", "",
        "Book", "Bible", "Safe",
    };

    public void printColor(Object object) {
        Class<? extends Object> c = object.getClass();
        while(true) {
            try {
                Field f = c.getField("ANSI_" + c.getName());
                print((String)f.get(object));
                return;
            } catch(Exception e) {
                //try next
                c = c.getSuperclass();
            }
        }
    }

    private synchronized void print(String s) {//private so final not used outside
        put.print(s);
    }

    void printSymbolName(Symbol s) {
        if(s == null) return;
        if(((Symbol)s).named != null) {
            printColor(s);
            printLiteral(s.named);
            print(" ");
        }
    }

    void printContext() {
        println();
        print(ANSI_RESET + "[");
        printSymbolName(current);
        print(ANSI_RESET + "] ");
        Book c = context;
        do {
            printSymbolName(c);
            c = c.in;
        } while(c.in != null);
        print(ANSI_RESET + "[");
        c = context;
        while(c.executeIn != null) {
            c = c.executeIn;
            printSymbolName(c);
        }
        print(ANSI_RESET + "]");
        println();
    }

    void list(Multex m, boolean newline) {
        if(m == null) return;
        if(newline) println();
        if(m instanceof Symbol) printSymbolName((Symbol)m);
        Book c = context;
        if(m instanceof Book) {
            context = (Book)m;//set self to view
        }
        for(int i = 0; i < m.basis.length; i++) {
            if(!(m instanceof UnitSymbol)) {
                if(i == m.idx) {
                    //cursor
                    printSymbolized("@");
                }
            }
            Symbol x = find(m.basis[i], false);
            if(x != null) {
                printSymbolName(x);
            } else {
                printSymbolized(m.basis[i]);//not found in context
            }
        }
        context = c;
        print(" ");
    }

    void printSymbolized(String s) {
        if(s == null) return;
        printColor(s);
        printLiteral(join(singleton(s)));//Mutex entry form
        print(" ");
    }

    void printLiteral(String s) {
        if(s == null) return;
        if(html) {
            print(escapeHTML(s).replace(htmlPara, "&"));//fix up HTML
        } else {
            print(s);
        }
    }

    void printHTML(String s) {
        if(s == null) return;
        if(html) {
            print(s.replace(htmlPara, "&"));//fix up HTML
        } else {
            print(s);
        }
    }

    void printTag(String name, String classOpen, Symbol nameValue) {//else close
        if(name == null) return;
        print("</span><");
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
        print("><span>");
    }

    void printSpecialTag(String name) {
        if(name == null) return;
        print("</span><");
        printLiteral(name);
        print(" /><span>");
    }

    private synchronized void println() {
        if (html) {
            put.print("</span><br /><span>");//quick!!
        } else {
            put.println(ANSI_RESET);
        }
    }

    //allows transaction completion by ignorance of errors until a start transaction is needed
    void startTransaction() {
        if(out.checkError() || err.checkError()) {
            throw new RuntimeException();//baulk
        }
    }

    void startFlusher() {
        Main m = this;
        flusher = new Thread(() -> {
            while(!out.checkError() && flusher == Thread.currentThread()) {
                synchronized(m) {
                    out.flush();
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    //loop
                }
            }
        });
        flusher.start();//new thread id used as survivor
    }

    void exitFlusher() {
        flusher = null;
    }

    //=========================================== ADAPTION UTILS

    /**
     * Set the input and output streams on the main class before calling the main.
     * @param i input stream.
     * @param o output stream.
     * @return the main instance.
     */
    public static Main setIO(InputStream i, PrintStream o) {
        return setIO(i, o, o);
    }

    /**
     * Set the input and output streams on the main class before calling the main.
     * @param i input stream.
     * @param o output stream.
     * @param e output error stream.
     * @return the main instance.
     */
    public static Main setIO(InputStream i, PrintStream o, PrintStream e) {
        Main m = getMain();
        if(i != null) m.in = i;
        if(o != null) m.out = o;
        if(e != null) m.err = e;
        return m;
    }

    /**
     * Sets HTML mode. This uses tags for styling, and also prevents a system exit. This allows the
     * code to be used for websites so as to not exit the web server process.
     * @return the main instance.
     */
    public static Main setHTML() {
        Main m = getMain();
        m.html = true;
        for(String i: reflect) {
            Class<?> c = Main.class;
            try {
                Field f = c.getField("ANSI_" + i);
                f.set(m, "</span><span class=\"" + i + "\">");
            } catch (Exception e) {
                m.err.println("Can't set color field");
            }
        }
        return m;
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
     * @return the main instance.
     */
    public static Main makeSafe(String safeName, Map<String, String[]> params) {
        Main m = getMain();
        Safe s = new Safe(safeName.intern());//safes do not evaluate or execute anything inside
        m.reg(s, m.context);
        for(String i: params.keySet()) {//serves you right if you did not clean parameters as no intern()
            Symbol key = new Symbol(i, params.get(i));
            m.reg(key, s);//register keys in safe
        }
        return m;
    }

    /**
     * Make a new threaded process to process an input stream with code and env parameters.
     * @param what the input stream.
     * @param with the code string.
     * @param params the map of string keys to array of strings.
     * @param idx a task index starting point.
     * @return the input stream to chain into other processes.
     */
    public static InputStream processHTML(InputStream what, String with,
                                          Map<String, String[]> params, int idx) {
        return Waiter.getPrintWaiter(new Thread(() -> {
            Waiter out = Waiter.bind();
            Main.setIO(what, out.getPrintStream());
            Main.setHTML();//as it needs this for no system exit
            Main m = Main.makeSafe("env", params);
            m.reg(new Ref("task", new Multex(String.valueOf(idx))));
            Main.run(with);//may not exhaustively use input what
            out.drainAndClose(what);
        }), idx);
    }

    /**
     * Make a new threaded data inserter on an input stream with code and env parameters.
     * @param what the input stream.
     * @param tag the tag string not including angle brackets.
     * @param run the code string to run for an insert after the found tag.
     * @param params the map of string keys to array of strings.
     * @param idx a task index starting point.
     * @return the input stream to chain into other processes.
     */
    public static InputStream atSpecialTag(InputStream what, String tag, String run,
                                          Map<String, String[]> params, int idx){
        return Waiter.getPrintWaiter(new Thread(() -> {
            Waiter out = Waiter.bind();
            String tag2 = "<" + tag + " />";
            StringBuilder sb = new StringBuilder();
            int id = out.getProcessID();
            try {
                while(true) {
                    int i = what.read();
                    if (i != -1) sb.append(i);
                    if(i == '>' || i == -1) {
                        if (sb.substring(sb.length() - tag2.length()).equals(tag2) || i == -1) {
                            InputStream insert = processHTML(null, run, params, id);//start
                            out = Waiter.stream(insert, Waiter.stream(
                                    new ByteArrayInputStream(sb.toString().getBytes()),
                                    out.getPrintStream()).getPrintStream());//insert
                            if (i == -1) break;
                            out = Waiter.stream(atSpecialTag(what, tag, run, params, id++), out.getPrintStream());//nest insert
                            break;
                        }
                    }
                }
                Waiter.drainAndClose(what);
            } catch (Exception e) {
                //stream error
            }
        }), idx);
    }

    /**
     * Make a new threaded partition process on an input stream with code and env parameters.
     * @param what the input stream.
     * @param tag the tag string not including angle brackets.
     * @param run the code string to run for an insert after the found tag.
     * @param params the map of string keys to array of strings.
     * @param idx a task index starting point.
     * @return the input stream to chain into other processes.
     */
    public static InputStream printSpliterator(InputStream what, String tag, String run,
                                           Map<String, String[]> params, int idx) {
        return Waiter.getPrintWaiter(new Thread(() -> {
            Waiter out = Waiter.bind();
            String tag2 = "<" + tag + " />";
            StringBuilder sb = new StringBuilder();
            int id = out.getProcessID();
            try {
                while(true) {
                    int i = what.read();
                    if(i != -1) sb.append(i);
                    if(i == '>' || i == -1) {
                        if (sb.substring(sb.length() - tag2.length()).equals(tag2) || i == -1) {
                            InputStream insert = processHTML(new ByteArrayInputStream(sb.toString().getBytes()),
                                    run, params, id);//start
                            out = Waiter.stream(insert, out.getPrintStream());
                            if (i == -1) break;
                            out = Waiter.stream(printSpliterator(what, tag, run, params, id++), out.getPrintStream());//nest
                            break;
                        }
                    }
                }
                Waiter.drainAndClose(what);
            } catch(Exception e) {
                //stream error
            }
        }), idx);
    }

    /**
     * Allows use of an output filtering stream on an input stream. Could be useful with chains
     * and how input streams nicely match the data flow of parameters. Output streams as parameters
     * require the equivalent of a process wait and join, whereas a fork can happen with no
     * synchronization.
     * @param input the input stream.
     * @param clazz the class of the filter output stream.
     * @return an input stream to chain processing.
     */
    public static InputStream filterPrintStream(InputStream input,
                                                Class<? extends FilterOutputStream> clazz) {
        return Waiter.getPrintWaiter(new Thread(() -> {
            Waiter w = Waiter.bind();
            try {
                Constructor<?> constructor = clazz.getConstructor(OutputStream.class);
                Object instance = constructor.newInstance(w.getPrintStream());
                w = Waiter.stream(input, new PrintStream((OutputStream) instance));
                ((OutputStream) instance).flush();//don't close as that's the waiter's job.
                //that would close the waiter stream as a cascade.
                Waiter.drainAndClose(input);
            } catch (Exception e) {
                //end thread on exception
            }
        }), 0);
    }

    /**
     * Allows use of an input filter stream on a print stream.
     * @param output the print stream to adapt.
     * @param clazz the filter input stream class.
     * @return a new print stream.
     */
    public static PrintStream filterInputStream(PrintStream output,
                                                 Class<? extends FilterInputStream> clazz) {
        PipedOutputStream o = new PipedOutputStream();
        try {
            Constructor<?> constructor = clazz.getConstructor(InputStream.class);
            Object instance = constructor.newInstance(new PipedInputStream(o));
            Waiter.stream((InputStream) instance, output);
        } catch (Exception e) {
            //end thread on exception
        }
        return new PrintStream(o);
    }
}
