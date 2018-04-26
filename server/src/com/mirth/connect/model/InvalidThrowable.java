/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.DocumentReader;

public class InvalidThrowable extends Throwable {

    private static final Pattern STACK_TRACE_PATTERN = Pattern.compile("(?<method>[^\\(]*)(\\((?<file>[\\s\\S]*)\\))?");

    private String throwableXml;
    private String className;
    private String detailMessage;

    public InvalidThrowable(String preUnmarshalXml, DonkeyElement element, HierarchicalStreamReader reader) {
        this.throwableXml = preUnmarshalXml;

        // Reset the stream reader to the correct element
        while (reader != null && reader.underlyingReader() instanceof DocumentReader && ((DocumentReader) reader.underlyingReader()).getCurrent() instanceof Element && !((DocumentReader) reader.underlyingReader()).getCurrent().equals(element.getElement())) {
            reader.moveUp();
        }

        if (element.hasAttribute("class")) {
            className = element.getAttribute("class");
        } else {
            className = element.getTagName();
        }

        DonkeyElement detailMessageElement = element.getChildElement("detailMessage");
        if (detailMessageElement != null) {
            detailMessage = detailMessageElement.getTextContent();
        }

        DonkeyElement causeElement = element.getChildElement("cause");
        if (causeElement != null) {
            try {
                initCause(ObjectXMLSerializer.getInstance().deserialize(causeElement.toXml(), Throwable.class));
            } catch (DonkeyElementException e) {
                // Ignore
            }
        }

        DonkeyElement stackTraceElement = element.getChildElement("stackTrace");
        if (stackTraceElement != null) {
            List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();

            for (DonkeyElement trace : stackTraceElement.getChildElements()) {
                Matcher matcher = STACK_TRACE_PATTERN.matcher(trace.getTextContent());

                if (matcher.find()) {
                    String declaringClass = "";
                    String methodName = "";
                    String fileName = null;
                    int lineNumber = 0;

                    String method = matcher.group("method");
                    if (method != null) {
                        int index = method.lastIndexOf('.');
                        if (index >= 0) {
                            declaringClass = method.substring(0, index);
                            methodName = method.substring(index + 1);
                        } else {
                            declaringClass = method;
                        }
                    }

                    String file = matcher.group("file");
                    if (file != null) {
                        int index = file.lastIndexOf(":");
                        if (index >= 0) {
                            fileName = file.substring(0, index);
                            lineNumber = NumberUtils.toInt(file.substring(index + 1));
                        } else {
                            fileName = file;
                        }
                    }

                    stackTrace.add(new StackTraceElement(declaringClass, methodName, fileName, lineNumber));
                }
            }

            setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
        }
    }

    public String getThrowableXml() {
        return throwableXml;
    }

    @Override
    public String getMessage() {
        return detailMessage;
    }

    @Override
    public String toString() {
        String message = getLocalizedMessage();
        return (message != null) ? (className + ": " + message) : className;
    }
}
