package uk.co.kring;

import java.io.*;
import java.util.HashMap;

/**
 * A waiter for returning a print stream continuation.
 */
public class Waiter {

    protected static HashMap<Thread, Waiter> waiters = new HashMap<>();
    private PrintStream s;
    private Thread t;
    private int idx;//a process id

    public Waiter(PrintStream stream, Thread thread, int id) {
        s = stream;
        t = thread;
        idx = id;
    }

    public PrintStream getPrintStream() {
        if(t == null) return s;
        try {
            t.join();
            Main.getMain().threads.remove(t);//helps with gc
        } catch(Exception e) {
            //continue anyway
        }
        return s;
    }

    public int getProcessID() {
        return idx;
    }

    public static Waiter bind() {
        Waiter w = waiters.get(Thread.currentThread());
        waiters.remove(w);
        return w;
    }

    public static void register(Waiter w, Thread t) {
        waiters.put(t, w);
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
     * Used to get a stream to use as output which can then later be resolved to an input.
     * The stream supports auto close after the thread finishes.
     * @param thread is the thread to execute using the stream.
     * @param id is the id to extract for use in the thread.
     * @return the input stream from the print stream.
     */
    public static InputStream getPrintWaiter(Thread thread, int id) throws IOException {
        PrintStream p = thread != null ? new ThreadPipePrintStream(new PipedOutputStream()) :
                new PipePrintStream(new ByteArrayOutputStream());
        if(thread == null) {
            return inputPrintStream(p);
        }
        Waiter.register(new Waiter(p, null, id), thread);//for later bind
        Thread bg = new Thread(() -> {
            thread.start();
            try {
                thread.join();
            } catch(Exception e) {
                //continue anyway
            }
            p.close();//auto close
        });
        bg.start();
        return inputPrintStream(p);
    }

    static InputStream inputPrintStream(PrintStream p) throws IOException {
        if(p instanceof ThreadPipePrintStream) {
            return new PipedInputStream(((ThreadPipePrintStream)p).s);
        }
        if(p instanceof PipePrintStream) {
            return new ByteArrayInputStream(((PipePrintStream) p).s.toByteArray());
        }
        throw new IOException("Can't collapse: " + p.toString());
    }

    /**
     * A threaded utility to copy an input stream to an output stream.
     * @param i input stream.
     * @param o output stream.
     * @return the output stream waiter to chain into other processes.
     */
    public static Waiter stream(InputStream i, PrintStream o) {
        Thread bg = new Thread(() -> {
            int avail;
            int read = 0;
            byte[] t;
            try {
                do {
                    while ((avail = i.available()) > 0) {
                        t = new byte[avail];//end?
                        read = i.read(t, 0, avail);
                        o.write(t, 0, avail);
                    }
                    Thread.yield();//pause
                } while(read != -1);
            } catch(IOException e) {
                //end of stream
            }
        });
        bg.start();
        return new Waiter(o, bg, 0);
    }
}
