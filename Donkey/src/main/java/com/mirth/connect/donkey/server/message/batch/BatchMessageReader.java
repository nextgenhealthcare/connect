/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message.batch;

import java.io.Reader;
import java.io.StringReader;

public class BatchMessageReader implements BatchMessageSource {

    private Reader reader;

    public BatchMessageReader(Reader reader) {
        this.setReader(reader);
    }

    public BatchMessageReader(String message) {
        reader = new StringReader(message);
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }
}
