package uk.co.kring;

public class Main {

    public static void main(String[] args) {
        for(int i = 0; i < args.length; i++) {
            print(args[0]);
            execute(args);
        }
        println();
    }

    public static boolean execute(String[] s) {
        if(s[0] == null) return true;//no fail nulls
        String t = s[0];//save
        System.arraycopy(s, 1, s, 0, s.length - 1);
        s[s.length - 1] = null;//shifted
        //TODO

        return true;//default OK
    }

    public static void print(String s) {
        if(s == null) return;
        System.out.print(s+" ");
    }

    public static void println() {
        System.out.println();
    }
}
