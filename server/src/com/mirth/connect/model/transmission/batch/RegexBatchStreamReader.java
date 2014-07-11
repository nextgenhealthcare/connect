/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission.batch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;

public class RegexBatchStreamReader extends BatchStreamReader {

    private Pattern delimiterRegex;
    private Charset charsetEncoding;
    private boolean includeDelimiter;

    public RegexBatchStreamReader(InputStream inputStream, String delimiterRegex, String charsetEncoding, boolean includeDelimiter) {
        super(inputStream);
        setDelimiterRegex(delimiterRegex);
        setCharsetEncoding(charsetEncoding);
        this.includeDelimiter = includeDelimiter;
    }

    public Pattern getDelimiterRegex() {
        return delimiterRegex;
    }

    public void setDelimiterRegex(String delimiterRegex) {
        this.delimiterRegex = Pattern.compile(delimiterRegex);
    }

    public Charset getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = Charset.forName(charsetEncoding);
    }

    public boolean isIncludeDelimiter() {
        return includeDelimiter;
    }

    public void setIncludeDelimiter(boolean includeDelimiter) {
        this.includeDelimiter = includeDelimiter;
    }

    @Override
    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List<Byte> endBytesBuffer, int lastByte) throws IOException {
        // We need to decode the string every time to account for variable-width charset encodings
        String encodedString = new String(capturedBytes.toByteArray(), charsetEncoding);

        Matcher matcher = delimiterRegex.matcher(encodedString);
        if (matcher.find()) {
            // Get the bytes before the matched group, and include the group only optionally
            return encodedString.substring(0, includeDelimiter ? matcher.end() : matcher.start()).getBytes(charsetEncoding);
        }

        return null;
    }
}
