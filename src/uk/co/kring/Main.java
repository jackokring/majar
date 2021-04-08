package uk.co.kring;

public class Main {

    public static void main(String[] args) {
        int err = 1;
        for(int i = 0; i < args.length; i++) {
            print(ANSI_GREEN + args[0]);
            int t = execute(args);
            if(err * t < 0) break;//overflow of errors
            if(t != 0) err *= t;//prime errors
        }
        println();
        System.exit(err - 1);//a nice ...
    }

    public static int execute(String[] s) {
        if(s[0] == null) return 1;//no fail nulls
        String t = s[0];//save
        System.arraycopy(s, 1, s, 0, s.length - 1);
        s[s.length - 1] = null;//shifted
        //TODO

        //place
        //s[s.length - 1] = "";
        return 1;//default OK
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
