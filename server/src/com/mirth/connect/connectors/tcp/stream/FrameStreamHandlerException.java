/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp.stream;

import com.mirth.connect.util.TcpUtil;

public class FrameStreamHandlerException extends StreamHandlerException {

    public FrameStreamHandlerException() {
        super();
    }

    public FrameStreamHandlerException(String message) {
        super(message);
    }

    public FrameStreamHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameStreamHandlerException(Throwable cause) {
        super(cause);
    }

    public FrameStreamHandlerException(boolean start, byte[] expectedBytes, byte[] actualBytes) {
        super(constructMessage(start, expectedBytes, actualBytes));
    }

    public FrameStreamHandlerException(boolean start, byte[] expectedBytes, byte[] actualBytes, Throwable cause) {
        super(constructMessage(start, expectedBytes, actualBytes), cause);
    }

    private static String constructMessage(boolean start, byte[] expectedBytes, byte[] actualBytes) {
        return (start ? "Start" : "End") + " of message byte" + (expectedBytes.length == 1 ? "" : "s") + " (" + TcpUtil.getByteAbbreviation(expectedBytes) + ") not detected. " + (start ? "First" : "Last") + " byte" + (actualBytes.length == 1 ? "" : "s") + " received: " + TcpUtil.getByteAbbreviation(actualBytes);
    }
}
