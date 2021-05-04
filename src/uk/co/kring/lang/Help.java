package uk.co.kring.lang;

import java.util.ListResourceBundle;

/**
 * Translations of error messages.
 */
public class Help extends ListResourceBundle {

    /**
     * The error messages of the interpreter.
     */
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                { "reg", "Register the top of stack in the current book" },
                { "book", "Make a new book and set it current" },
                { "author", "Set the current book to the context" },
                { "context", "Show the context and words in closest context book" },
                { "current", "Show the words in the current book" },
                { "lit", "Place next word on stack literally" },
                { "find", "Find next word and place it on the stack" },
                { "delay", "Delay execution of next word. Maybe it's a macro" },
                { "eval", "Evaluate the top of stack" },
                { "nul", "A nul value. Indicates some error of programming" },
                { "ref", "Makes a reference in the current book to the top of stack" },
                { "space", "Saves the stack and makes a new stack space" },
                { "import", "Import the top of stack from the last saved space" },
                { "export", "Export the top of stack to the last saved space" },
                { "time", "Make a thread in the current book" },
                { "safe", "Make a safe (hash table) in the current book" },
                { "store", "Store the top of stack as a value keyed by the following name in the last used safe" },
                { "env", "An environmental variable safe" },
                { "input", "Read a line from the input and stack it" },
                { "accept", "Read a line from the input but use the following literal if there is no input" },
                { "direct", "Start a read and evaluate loop" },
                { "exit", "End a read and evaluate loop" },
                { "abort", "Abort process with an error" },
                { "party", "Exit the process with no error" },
                { "print", "Print the top of stack" },
                { "html", "Set HTML mode" },
                { "ansi", "Set ANSI color mode" },
                { "show", "Show all the stack list" },
                { "list", "List the next literal word as found" },
                { "name", "Show the name of the top of stack" },
                { "dollar", "Replace $ in the the top of stack with lower stack items" },
                { "source", "Read in a whole source file and execute it" },
                { "true", "A universal true value evaluating to itself" },
                { "false", "A true symbol which evaluates to false" },
                { "elucidate", "If the top of stack is false returns a printable truth of the false" },
                { "not", "Logical not" },
                { "xor", "Logical xor" },
                { "or", "Logical or. Top most truth has priority" },
                { "and", "Logical and. Top most truth has priority" },
                { "imp", "Logical implication" },
                { "same", "Test if two things are the same" },
                { "binequal", "Test if two things have same boolean value" },
                { "begin", "Start a literal sequence to stack ended by end" },
                { "end", "Ends a literal sequence. Delay may escape a literal end" },
                { "nest", "Like begin but places the literal sequence escaped inline" },
                { "def", "Defines a symbol to later execute. Ended by end" },
                { "para", "Gets a literal parameter from a calling symbol" },
                { "many", "Gets a literal sequence parameter. Must start with begin and end with end" },
                { "omit", "Omit the next word on error" },
                { "catch", "Run the next word on error" },
                { "ok", "Clear errors" },
                { "while", "While top of stack is true run until end" },
                { "fast", "Hides trace information" },
                { "slow", "Shows trace information" },
                { "force", "Forces an immediate fail if there was an error" },
                { "later", "Run the rest of the words after returning and finishing the caller word" },
                { "continue", "In loops continues. Does a return when not in a loop" },
                { "break", "In a loop breaks. Does a double return when not in a loop" },
                { "after", "Schedule the top of stack for execution after this" },
                { "outer", "Remove the next after from the schedule and place it on stack" },
                { "until", "Until top of stack is true run until end. Leaves truth of exit on stack" },
                { "ignore", "Ignore until end" },
                { "dup", "Duplicate the top of stack" },
                { "drop", "Consume the top of stack" },
                { "over", "Make a copy of the second on stack and place it on top" },
                { "swap", "Swap the top two things on the stack" },
                { "nip", "Consume the second thing on stack" },
                { "tuck", "Make a copy of the top of stack and tuck it below the second on stack" },
                { "if", "If top of stack true run until end" },
                { "either", "If top of stack true run until end leaving top of stack no consumed" },
                { "else", "If top of stack false run until end" },
                { "yield", "Indicate a low priority of threading" },
                { "empty", "Get a boolean to test if the stack is empty" },
                { "exec", "Perform a quiet system exec call with the top of stack" },
        };
    }
}

