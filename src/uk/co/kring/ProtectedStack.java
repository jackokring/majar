package uk.co.kring;

import java.util.Stack;

/**
 * An error checked stack for the interpreter.
 * @param <T> the stack unit element kind.
 */
public class ProtectedStack<T> extends Stack<T> {

    public final int STACK_MAX = 1024;
    private int count = 0;
    private T under;

    public ProtectedStack(T onUnder) {
        under = onUnder;
    }

    @Override
    public T push(T t) {
        if(t == Main.nul) return t;//prevents nul flood
        if(count++ > STACK_MAX) Main.getMain().setError(Main.ERR_OVER, this);
        return super.push(t);

    }

    @Override
    public synchronized T pop() {
        if(empty()) {
            Main.getMain().setError(Main.ERR_UNDER, this);
            return under;
        }
        count--;
        return super.pop();
    }
}
