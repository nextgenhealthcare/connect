/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.builders;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.mozilla.javascript.RhinoException;

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
            stackTrace = ExceptionUtils.getStackTrace(e);
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
