package com.mirth.connect.connectors.tcp.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexBatchStreamHandler extends StreamHandler {

    private Pattern delimiterRegex;
    private Charset charsetEncoding;
    private boolean includeDelimiter;

    public RegexBatchStreamHandler(InputStream inputStream, String delimiterRegex, String charsetEncoding, boolean includeDelimiter) {
        this(inputStream, delimiterRegex, charsetEncoding, includeDelimiter, new byte[] {}, new byte[] {});
    }

    public RegexBatchStreamHandler(InputStream inputStream, String delimiterRegex, String charsetEncoding, boolean includeDelimiter, byte[] beginBytes, byte[] endBytes) {
        this(inputStream, delimiterRegex, charsetEncoding, includeDelimiter, beginBytes, endBytes, false);
    }

    public RegexBatchStreamHandler(InputStream inputStream, String delimiterRegex, String charsetEncoding, boolean includeDelimiter, byte[] beginBytes, byte[] endBytes, boolean returnDataOnException) {
        super(inputStream, null, beginBytes, endBytes, returnDataOnException);
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
    protected byte[] checkForIntermediateMessage() throws IOException {
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
