package uk.co.kring;

import java.util.Stack;

public class PStack<T> extends Stack<T> {

    @Override
    public T push(T t) {
        try {
            return super.push(t);
        } catch(OutOfMemoryError e) {
            System.gc();
            Main.setError(Main.ERR_OVER, this);
            return t;
        }
    }

    @Override
    public synchronized T pop() {
        if(empty()) Main.setError(Main.ERR_UNDER, this);
        return super.pop();
    }
}
