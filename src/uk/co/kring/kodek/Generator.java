package uk.co.kring.kodek;

/**
 * A bit bias generator using PQ asymmetry sequencing.
 */
public class Generator {
    //TODO: thought on Mobius.

    boolean randomNotSample = true;
    //boolean reflectionParity = false;
    boolean clockwise = true;

    final int max = Integer.MAX_VALUE;
    int p;
    int q;

    Chaos gen;

    public Generator(Chaos c, int hi) {
        gen = c;
        p = hi;
        q = max - hi;
    }

    boolean value() {
        return clockwise;// ^ reflectionParity;
    }

    void next() {
        if(randomNotSample) {
            if(clockwise) {
                if(p < gen.next()) {
                    randomNotSample = !randomNotSample;//clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            } else {
                if(q < gen.next()) {
                    randomNotSample = !randomNotSample;//anti-clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            }
        } else {
            if(clockwise) {
                if(q < gen.next()) {
                    randomNotSample = !randomNotSample;//clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            } else {
                if(p < gen.next()) {
                    randomNotSample = !randomNotSample;//anti-clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            }
        }
    }

    void prev() {
        gen.next();
        if(randomNotSample) {
            if(!clockwise) {
                if(p < gen.prev()) {
                    randomNotSample = !randomNotSample;//clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            } else {
                if(q < gen.prev()) {
                    randomNotSample = !randomNotSample;//anti-clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            }
        } else {
            if(!clockwise) {
                if(q < gen.prev()) {
                    randomNotSample = !randomNotSample;//clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            } else {
                if(p < gen.prev()) {
                    randomNotSample = !randomNotSample;//anti-clockwise motion
                } else {
                    clockwise = !clockwise;//reflection
                    //reflectionParity = !reflectionParity;//parity
                }
            }
        }
        gen.prev();
    }

    boolean postNext() {
        if(randomNotSample) {
            boolean rand = (gen.next() & 1) != 0;//randomize
            //boolean rand2 = (gen.next() & 1) != 0;//randomize
            clockwise ^= rand;
            //reflectionParity ^= rand2;//align to same start polarity
            return false;//reflectionParity;//false;
        }
        return true;//value OK
    }

    void prePrev() {
        gen.next();
        if(randomNotSample) {
            //boolean rand2 = (gen.next() & 1) != 0;//randomize
            boolean rand = (gen.prev() & 1) != 0;//randomize
            clockwise ^= rand;
            //reflectionParity ^= rand2;//align to same start polarity
        }
        gen.prev();
    }

    boolean postPrev() {
        if(randomNotSample) {
            return false;
        }
        return true;//reflectionParity;//true;//value OK
    }

    //there is a coding if the entropy exceeds a 33:66-ish bias
    //as like 3 dice with 1,2:3,4,5,6 and a majority count increases bias
    //then a set of generators implies enough bias given enough speed or silicon area.
    void modulate() {
        clockwise = !clockwise;
    }

    //the entropy of this stream should not be 50:50 or 1 bit per bit
    boolean forward() {
        boolean test;
        do {
            next();
            test = postNext();
        } while(!test);
        return value();
    }

    //this should be the logical time reversal of the forward() method.
    //implying a stack storage on the magnified bias.
    boolean reverse() {
        boolean test;
        do {
            prePrev();
            prev();
            test = postPrev();
        } while(!test);
        return value();
    }
}
