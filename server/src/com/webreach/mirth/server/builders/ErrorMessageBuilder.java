package com.webreach.mirth.server.builders;

import org.mozilla.javascript.RhinoException;

import com.webreach.mirth.server.util.StackTracePrinter;

public class ErrorMessageBuilder {
    private String lineSeperator = System.getProperty("line.separator");

    public String buildErrorMessage(String errorType, String customMessage, Throwable e) {
        // error source is initialized to blank
        String lineSource = null;

        // if the exception occured during execution of the script, get the
        // line of code that caused the error
        if (e instanceof RhinoException) {
            RhinoException re = (RhinoException) e;
            lineSource = re.lineSource();
        }

        // construct the error message
        StringBuilder errorMessage = new StringBuilder();
        String stackTrace = new String();
        
        if (e != null) {
            stackTrace = StackTracePrinter.stackTraceToString(e);
        }
        
        errorMessage.append(errorType + lineSeperator);

        if ((lineSource != null) && (lineSource.length() > 0)) {
            errorMessage.append("ERROR SOURCE:\t" + lineSource + lineSeperator);
        }

        if ((customMessage != null) && (customMessage.length() > 0)) {
            customMessage += lineSeperator;
            errorMessage.append("ERROR MESSAGE:\t" + customMessage + stackTrace + lineSeperator);
        } else {
            errorMessage.append(stackTrace + lineSeperator);
        }

        return errorMessage.toString();
    }
}
