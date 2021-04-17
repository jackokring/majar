package uk.co.kring;

import java.util.Stack;

/**
 * An error checked stack for the interpreter.
 * @param <T> the stack unit element kind.
 */
public class ProtectedStack<T> extends Stack<T> {

    @Override
    public T push(T t) {
        try {
            return super.push(t);
        } catch(OutOfMemoryError e) {
            System.gc();
            Main.getMain().setError(Main.ERR_OVER, this);
            return t;
        }
    }

    @Override
    public synchronized T pop() {
        if(empty()) Main.getMain().setError(Main.ERR_UNDER, this);
        return super.pop();
    }
}