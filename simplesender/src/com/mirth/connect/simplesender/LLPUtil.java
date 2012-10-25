package com.mirth.connect.simplesender;

public class LLPUtil {

    private static final char END_MESSAGE = '\u001c'; // character indicating
                                                      // the
    // termination of an HL7 message
    private static final char START_MESSAGE = '\u000b';// character indicating
                                                       // the
    // start of an HL7 message
    private static final char LAST_CHARACTER = 13; // the final character of

    private static final String VERTICAL_TAB = "\\[SOM\\]";
    private static final String CARRIAGE_RETURN = "\\[CR\\]";
    private static final String END_OF_MESSAGE = "\\[EOM\\]";

    // a message: a carriage return
    /*
     * Returns HL7 encoded message
     */
    public static String HL7Encode(String message) {
        if (message.indexOf(VERTICAL_TAB.replaceAll("\\\\", "")) != -1)
            return message.replaceAll(VERTICAL_TAB, START_MESSAGE + "").replaceAll(CARRIAGE_RETURN, LAST_CHARACTER + "").replaceAll(END_OF_MESSAGE, END_MESSAGE + "");
        else
            return START_MESSAGE + message + END_MESSAGE + LAST_CHARACTER;
    }
}
