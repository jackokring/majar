package uk.co.kring.kodek;

import java.util.Random;

/**
 * A reversible pseudo random bit generator.
 */
public class Chaos {

    long _a;
    long _c;
    long _m;
    long _s;
    long _i;

    public Chaos() {
        this(0, 0);
    }

    public Chaos(int prod, int add) {
        _a = prod == 0 ? 1664525 : prod;
        _c = add == 0 ? 1013904223 : add;
        _s = new Random().nextLong() * 2 + 1;//odd
        _m = (long)Integer.MAX_VALUE + 1;
        _s = _s % _m;
        next();// a fast round
        _i = modInverse(_a, _m);//4276115653 as inverse of 1664525
    }

    long next() {
        return _s = (_a * _s + _c) % _m;
    }

    long prev() {
        return _s = (_s - _c) * _i % _m;
    }

    public static long modInverse(long a, long m) {
        long m0 = m;
        long y = 0, x = 1;
        if (m == 1) return 0;
        while (a > 1) {
            // q is quotient
            long q = a / m;
            long t = m;
            // m is remainder now, process
            // same as Euclid's algo
            m = a % m;
            a = t;
            t = y;
            // Update x and y
            y = x - q * y;
            x = t;
        }
        // Make x positive
        if (x < 0)
            x += m0;
        return x;
    }

    public static long gcd(long r, long s) {
        while (s != 0) {
            long t = s;
            s = r % s;
            r = t;
        }
        return r;
    }
}
