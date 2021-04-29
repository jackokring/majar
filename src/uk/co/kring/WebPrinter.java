package uk.co.kring;

import java.io.OutputStream;
import java.io.PrintStream;

import static uk.co.kring.Main.htmlPara;

/**
 * A class for implementing some easier output desires for the web.
 */
public class WebPrinter extends PrintStream {

    public WebPrinter(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Print a general HTML tag. Also closes any span.
     * @param name name of tag.
     * @param classOpen the class of the tag.
     * @param nameValue the name and value fields of the tag.
     */
    public void printTag(String name, String classOpen, Symbol nameValue) {//else close
        if(name == null) return;
        print("</span><");
        if(classOpen == null) print("/");
        printLiteral(name);
        if(classOpen != null) {
            print(" class=\"");
            printLiteral(classOpen);
            print("\"");
        }
        if(nameValue != null) {
            print(" name=\"");
            printLiteral(nameValue.named);
            print("\" ");
            print(" value=\"");
            printLiteral(Main.join(nameValue.basis));
            print("\"");
        }
        print("><span>");
    }

    /**
     * Print a self closing tag with a name.
     * @param name the string to print.
     */
    public void printSpecialTag(String name) {
        if(name == null) return;
        print("</span><");
        printLiteral(name);
        print(" /><span>");
    }

    /**
     * Print text so that it is viewed. Shows HTML tags how they would
     * be written, not how they would be rendered as tags with function.
     * @param s the string to print.
     */
    public void printLiteral(String s) {
        if(s == null) return;
        print(Main.escapeHTML(s).replace(htmlPara, "&"));//fix up HTML
    }

    /**
     * Print text so that it is rendered. Shows HTML tags
     * how they would be rendered as tags with function.
     * @param s the string to print.
     */
    public void printMarked(String s) {
        if(s == null) return;
        print(s.replace(htmlPara, "&"));//fix up HTML
    }
}
