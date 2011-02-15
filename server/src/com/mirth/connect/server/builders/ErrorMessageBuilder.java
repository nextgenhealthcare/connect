/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.builders;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.mozilla.javascript.RhinoException;

public class ErrorMessageBuilder {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public String buildErrorMessage(String errorType, String customMessage, Throwable e) {
        String errorSourceLine = null;

        // if the exception occured during execution of the script, get the
        // line of code that caused the error
        if (e instanceof RhinoException) {
            errorSourceLine = ((RhinoException) e).lineSource();
        }

        // construct the error message
        StringBuilder builder = new StringBuilder();
        String stackTrace = new String();

        if (e != null) {
            stackTrace = ExceptionUtils.getStackTrace(e);
        }

        builder.append(errorType + LINE_SEPARATOR);

        if (StringUtils.isNotBlank(errorSourceLine)) {
            builder.append("ERROR SOURCE:\t");
            builder.append(errorSourceLine + LINE_SEPARATOR);
        }

        if (StringUtils.isNotBlank(customMessage)) {
            builder.append("ERROR MESSAGE:\t");
            builder.append(customMessage + LINE_SEPARATOR);
            builder.append(stackTrace + LINE_SEPARATOR);
        } else {
            builder.append(stackTrace + LINE_SEPARATOR);
        }

        return builder.toString();
    }
}
