package uk.co.kring;

public class Bible extends Book {

    public Bible() {
        super("bible".intern());
        in = null;//null terminal on context
        build();
    }

    void build() {
        //TODO main bible hook point
    }
}
