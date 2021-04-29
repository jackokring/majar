package uk.co.kring;

import java.io.*;
import java.util.HashMap;

/**
 * A waiter for returning a print stream continuation.
 */
public class Waiter {

    private final static HashMap<Thread, Waiter> waiters = new HashMap<>();
    private final PrintStream s;
    private Thread t;
    private final int idx;//a process id

    Waiter(PrintStream stream, Thread thread, int id) {
        s = stream;
        t = thread;
        idx = id;
    }

    /**
     * Gets the print stream from the waiter after waiting on the bound thread finishing.
     * @return the print stream.
     */
    public PrintStream getPrintStream() {
        if(t == null) return s;
        try {
            t.join();
            synchronized (Waiter.class) {
                Main.deleteMain(t);//helps with gc
                //t = Thread.currentThread();//re-lock on current thread
                t = null;//self managed sequentially then on
            }
        } catch(Exception e) {
            //continue anyway
        }
        return s;
    }

    /**
     * Gets the id bound within the waiter if any.
     * @return the id or zero.
     */
    public int getProcessID() {
        return idx;
    }

    /**
     * Get a bound waiter for the output stream piped into the input stream returned by getting a print
     * waiter.
     * @return the waiter.
     */
    public static synchronized Waiter bind() {
        Waiter w = waiters.get(Thread.currentThread());
        waiters.remove(Thread.currentThread());
        return w;
    }

    static synchronized void register(Waiter w, Thread t) {
        waiters.put(t, w);
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
    public static InputStream getPrintWaiter(Thread thread, int id) {
        ThreadPipePrintStream p = new ThreadPipePrintStream(new PipedOutputStream());
        Waiter.register(new Waiter(p, null, id), thread);//for later bind no join
        Thread bg = new Thread(() -> {
            thread.start();
            try {
                thread.join();
            } catch(Exception e) {
                //continue anyway
            }
            p.close();//auto close
        });
        if(thread != null) {
            bg.start();
        } else {
            p.close();
            return new ByteArrayInputStream(ERR_STREAM.getBytes());
        }
        try {
            return new PipedInputStream(p.s);
        } catch(IOException e) {
            p.close();
            return new ByteArrayInputStream(ERR_PIPE.getBytes());
        }
    }

    public static final String ERR_PIPE = "<!-- Can't read pipe -->";
    public static final String ERR_STREAM = "<!-- No thread writes pipe -->";

    /**
     * A threaded utility to copy an input stream to an output stream.
     * @param i input stream.
     * @param o output stream.
     * @return the output stream waiter to chain into other processes.
     */
    public static Waiter stream(InputStream i, PrintStream o) {
        Thread bg = new Thread(() -> {
            int avail;
            int read;
            byte[] t;
            try {
                do {
                    avail = i.available();
                    t = new byte[avail];//end?
                    read = i.read(t, 0, avail);
                    o.write(t, 0, avail);
                    Thread.yield();//pause
                } while(read != -1);
            } catch(IOException e) {
                //end of stream
            }
            drainAndClose(i);
        });
        bg.start();
        return new Waiter(o, bg, 0);//stream join on copy complete
    }

    /**
     * Drain and close an input stream.
     * @param in the stream.
     */
    public static void drainAndClose(InputStream in) {
        try {
            in.close();
        } catch(IOException e) {
            //back propagation cull
        }
    }

    /**
     * Test to see if starting a transaction is a good idea.
     * @return true if commit is necessary as a block free of IO problems.
     */
    public boolean startTransaction() {
        return !s.checkError();
    }
}
