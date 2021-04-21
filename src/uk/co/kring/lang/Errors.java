package uk.co.kring.lang;

import java.util.ListResourceBundle;

/**
 * Translations of error messages.
 */
public class Errors extends ListResourceBundle {

    /**
     * The error messages of the interpreter.
     */
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                { "0", "Input or output problem. Was the process interrupted? OK" },           //0
                { "1", "Stack underflow. Not enough data was provided to some word" }, //1
                { "2", "Out of memory. Maybe the stack overflowed" },  //2
                { "3", "Closing \" missing. Check your code" },      //3
                { "4", "External process error." },//4
                { "5", "Word not found. The word is not defined and in a book" },       //5
                { "6", "Protected f'ing bible. The bible book has reserved words in" },//6
                { "7", "Raise you an irrefutable. Yes, the bible can't be revoked, but can be expanded" },//7
                { "8", "Bad context. There is a definition but not in the context chain. Use context" },     //8
                { "9", "Bad plugin. The Java class to provide a word as a context plugin is not a class extending Prim" },  //9
                { "10", "No! You can't alter the bible in that way. Consider forking and editing the Java Bible class build method" },     //10
                { "11", "Quoted string formatted bad. Do not use \" in the middle of words and leave spaces" },   //11
                { "12", "Symbol with no name. A symbol must have a name to write it into a book" },   //12
                { "13", "Overwritten book. All the words in it are now gone" },  //13
                { "14", "Partial context deleted. Some books in the context chain no longer exist" },  //14
                { "15", "Current book deleted. Current book set to the bible" },  //15
                { "16", "Multiple books deleted. A large deletion of books happened" },  //16
                { "17", "A bad execution context. The book was deleted. 'Tis but a crust" },     //17
                { "18", "Can't multi-thread this. Something refused to duplicate and provide unique per thread storage" }, //18
                { "19", "Macro terminal excess. Some words must have preceding words. Like code brackets" }, //19
                { "20", "Nul terminal problems. You're crazy. Stop pretending things are still on stacks" },   //20
                { "21", "Chaos in progress. Given your fancy for using nul, here goes ..."}, //21

                { "abort", "User aborted process." },
        };
    }
}
