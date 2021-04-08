package uk.co.kring;

public class Main {

    private static int err, last;//primary error code

    public static void main(String[] args) {
        clearErrors();
        for(int i = 0; i < args.length; i++) {
            print(ANSI_GREEN + args[0]);
            execute(args);
            if(errOver()) break;//prime errors
        }
        println();
        printErr();
        System.exit(err - 1);//a nice ...
    }

    public static void execute(String[] s) {
        if(s[0] == null) return;//no fail nulls
        String t = s[0];//save
        System.arraycopy(s, 1, s, 0, s.length - 1);
        s[s.length - 1] = null;//shifted
        //TODO

        //place
        //s[s.length - 1] = "";
    }

    static final String[] errorFact = {
        "Non specific error"            //1
    };

    static final int[] errorCode = {//by lines of 4
        2, 3, 5, 7,                     //1
        11, 13, 17, 19                  //4
    };

    static final int[] errorComposites = {
        //compositeErrorCode, errorFact : pair per reduction
    };

    static void clearErrors() {
        err = 1;
        last = -1;
    }

    public static void setError(int t) {
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
        return err * last < 0;
    }

    public static void printErr() {
        if(last >= 0) System.err.println(ANSI_RED + errorFact[last]);
        String c = ANSI_YELLOW;
        if(errOver()) c = ANSI_RED;//many errors
        else err /= errorCode[last];//divide out last
        for(int i = 0; i < errorFact.length; i++) {
            if(err == 1) break;
            if(err % errorCode[i] == 0) {
                System.err.println(c + errorFact[i]);
                err /= errorCode[i];
            }
        }
        System.err.println(ANSI_RESET);
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
