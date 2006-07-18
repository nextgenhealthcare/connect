package com.webreach.mirth.testbench;

public class LLPUtil 
{

    private static final char END_MESSAGE = '\u001c'; //character indicating the
                                              //termination of an HL7 message
    private static final char START_MESSAGE = '\u000b';//character indicating the
                                               //start of an HL7 message
    private static final char LAST_CHARACTER = 13; //the final character of
        //a message: a carriage return
    /*
     * Returns HL7 encoded message
     */
    public static String HL7Encode (String message)
    {
    	return START_MESSAGE + message + END_MESSAGE + LAST_CHARACTER;
    }
}

