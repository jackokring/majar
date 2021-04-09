package uk.co.kring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Stack;

public class Main {

    private static int err, last, first;//primary error code
    static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    static Stack<Multex> ret = new Stack<>();
    static Stack<Multex> dat = new Stack<>();

    public static void main(String[] args) {
        if(ret.empty()) {
            clearErrors();
            intern(args);//first
        }
        ret.push(new Multex(args));
        for(int i = 0; i < args.length; i++) {
            print(ANSI_GREEN + ret.peek().firstString());
            execute();
            if(errOver()) break;//prime errors
        }
        ret.pop();
        if(ret.empty()) {
            println();
            printErr();
            System.exit(first);//a nice ...
        }
    }

    //========================================== INTERPRETER

    public static void execute() {
        Multex s = ret.peek();
        if(s.firstString() == null) return;//no fail null
        s.run();
        s.shift();
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
            setError(ERR_IO);//Input
            return new String[0];//blank
        }
    }

    //================================================== STRING UTIL

    public static void intern(String[] s) {
        for(int i = 0; i < s.length; i++) {
            s[i] = s[i].intern();//pointers??
        }
    }

    //================================================== ERRORS

    static final String[] errorFact = {
        "Input"            //0
    };

    public static final int ERR_IO = 0;

    static final int[] errorCode = {//by lines of 4
        2, 3, 5, 7,                     //0
        11, 13, 17, 19                  //4
    };

    static final int[] errorComposites = {
        //compositeErrorCode, errorFact : pair per reduction
    };

    static void clearErrors() {
        err = 1;
        last = -1;
        first = 0;
    }

    public static void setError(int t) {
        if(first < 1) first = errorCode[t];
        last = t;
        t = errorCode[t];//map
        if(err * t < 0 || t == 0) return;
        err *= t;
        mapErrors();
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

    public static void printErr() {
        if(last != -1) {
            errorPlump(ANSI_RED, last);
            String c = ANSI_YELLOW;
            if(errOver()) c = ANSI_RED;//many errors
            else {
                first = err;//return all if no over
                if(first == 1) first = 0;//no error
                if(last != -1) err /= errorCode[last];//divide out last
            }
            for(int i = 0; i < errorFact.length; i++) {
                if(err == 1) break;
                if(err % errorCode[i] == 0) {
                    errorPlump(c, i);
                    err /= errorCode[i];
                }
            }
            System.err.println(ANSI_RESET);
        }
        last = -1;//errors flushed
    }

    static final String errorWord = " error.";

    static void errorPlump(String prefix, int code) {
        System.err.println(prefix +
                "[" + errorCode[code] + "] " +
                errorFact[code] + errorWord);
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
