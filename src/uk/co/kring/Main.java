package uk.co.kring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Main {

    private static int err, last, first;//primary error code
    static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    static Stack<Multex> ret = new PStack<>();
    static Stack<Multex> dat = new PStack<>();
    static HashMap<String, List<Symbol>> dict =
            new HashMap<>();
    static Book context;
    static Book current;

    //========================================== ENTRY / EXIT

    public static void main(String[] args) {
        if(ret.empty()) {
            clearErrors();
            intern(args);//first
        }
        execute(new Multex(args));
        if(ret.empty()) {
            println();
            printErrorSummary();
            System.exit(first);//a nice ...
        }
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
            print(ANSI_GREEN + s.firstString());
            s.run();
            s.shift();
            if(errOver()) break;//prime errors
        }
        ret.pop();
    }

    static final String para = "\\~";//quirk of the shell

    public static String[] readLine(BufferedReader in) {
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
            intern(args);//pointers??
            return args;
        } catch (Exception e) {
            setError(ERR_IO, System.in);//Input
            return new String[0];//blank
        }
    }

    public static String[] readString(String in) {
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

            String j = topMost(dat).replace("$", para);//recursive
            s = s.substring(0, i) + j + s.substring(i + 1);
        }
        return s.replace(para, "$");
    }

    public static String topMost(Stack<Multex> sm) {
        Multex m = sm.peek();
        while(m.firstString() == null) {
            m.shift();
            if(m.ended()) m = sm.pop();
        }
        String s = m.firstString();
        m.shift();
        return s;
    }

    public static String parameter(Stack<Multex> sm) {
        Multex m = sm.pop();
        String s = topMost(sm);
        sm.push(m);
        return s;
    }

    public static void swap(Stack<Multex> sm) {
        Multex m = sm.pop();
        Multex t = sm.pop();
        sm.push(m);
        sm.push(t);
    }

    //================================================== ERRORS

    static final String[] errorFact = {
        "Input",           //0
        "Stack underflow", //1
        "Stack overflow"   //2
    };

    public static final int ERR_IO = 0;
    public static final int ERR_UNDER = 1;
    public static final int ERR_OVER = 2;

    static final int[] errorCode = {//by lines of 4
        2, 3, 5, 7,                     //0
        11, 13, 17, 19                  //4
    };

    static final int[] errorComposites = {
        //compositeErrorCode, errorFact : pair per reduction
        561, 8
    };

    static void clearErrors() {
        err = 1;
        last = -1;
        first = 0;
    }

    public static void setError(int t) {
        setError(t, null);
    }

    public static void setError(int t, Object o) {
        String s;
        if(o == null) {
            s = "No further data";
        } else {
            s = classNamed(o);
        }
        errorPlump(ANSI_RED, t, s);
        if(first < 1) first = errorCode[t];
        last = t;
        t = errorCode[t];//map
        if(err * t < 0 || t == 0) return;
        err *= t;
        mapErrors();
    }

    public static String classNamed(Object o) {
        if(o instanceof String) return ANSI_RESET + o;
        if(o instanceof Symbol) return ANSI_BLUE + o.getClass().getName() + ": " + classNamed(((Symbol)o).named);
        return ANSI_PURPLE + o.getClass().getName() + " [" + Integer.toHexString(o.hashCode()) + "]";
    }

    static void mapErrors() {
        for(int i = 0; i < errorComposites.length; i += 2) {
            if(err % errorComposites[i] == 0) {
                err /= errorComposites[i];
                setError(errorComposites[i + 1]);//apply the composite and reduce
            }
        }
    }

    static boolean errOver() {
        if(last < 0) return false;
        return err * last < 0;
    }

    public static void printErrorSummary() {
        if(last != -1) {
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
            System.err.print(ANSI_RESET);
        }
        last = -1;//errors flushed
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
        System.out.print(s+" ");
    }

    public static void println() {
        System.out.println(ANSI_RESET);
    }
}
