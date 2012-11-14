package com.mirth.connect.connectors.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.connectors.tcp.stream.DelimiterBatchStreamHandler;
import com.mirth.connect.connectors.tcp.stream.ER7BatchStreamHandler;
import com.mirth.connect.connectors.tcp.stream.RegexBatchStreamHandler;
import com.mirth.connect.connectors.tcp.stream.StreamHandler;

public class StreamHandlerTests {

    public static byte[] llpStartBytes = new byte[] { 0x0B };
    public static byte[] llpEndBytes = new byte[] { 0x1C, 0x0D };
    public static String testMessage = "MSH|^~\\&|ADT1|SHM|SHMADT|SHM|200812091126|SECURITY|ADT^A01^ADT_A01|MSG00001|P|2.5|\rEVN|A01|200812091126||\rPID|1|1001|1001^5^M11^ADT1^MR^SHM~123456789^^^USSSA^SS||O'HALLAHAN^COLLEEN^^||19850704|F||2106-3|1200 N ELM STREET^^NEWPORT BEACH^CA^92660-1020^US^H|OC|(949) 555-1234|(949) 555-5678||S||PATID1001^2^M10^ADT1^AN^A|123456789|U1234567^CA|\rNK1|1|O'HALLAHAN^BRITTANY^M|SIS^SISTER||||N^NEXT-OF-KIN\rPV1|1|I|2000^2012^01||||001122^ZOIDBERG^JOHN^|||SUR||||1|A0|\r";
    public static String testMessageCharset = "US-ASCII";
    public static byte[] testMessageBytes;
    public static int testBufferSize = 65536;

    @BeforeClass
    public static void beforeClass() throws Exception {
        testMessageBytes = testMessage.getBytes(testMessageCharset);
    }

    @Test
    public void readSingleMessage() throws Exception {
        byte[] testBytes = testMessageBytes;
        StreamHandler streamHandler;
        ByteArrayOutputStream bos;
        InputStream is;
        int maxRead;

        // Create regular LLP frame with HL7 message
        bos = new ByteArrayOutputStream();
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, false);
        // Assert that the bytes returned from the stream handler are correct
        assertTrue(Arrays.equals(testBytes, streamHandler.getNextMessage()));

        // Create LLP frame with extra bytes at the beginning
        bos = new ByteArrayOutputStream();
        bos.write("Testing".getBytes(testMessageCharset));
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, false);
        // Assert that the bytes returned from the stream handler are correct
        assertTrue(Arrays.equals(testBytes, streamHandler.getNextMessage()));

        // Create LLP frame with extra bytes at the end
        bos = new ByteArrayOutputStream();
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        bos.write("Testing".getBytes(testMessageCharset));
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, false);
        // Assert that the bytes returned from the stream handler are correct
        assertTrue(Arrays.equals(testBytes, streamHandler.getNextMessage()));

        // Create LLP frame with extra bytes at the beginning and end
        bos = new ByteArrayOutputStream();
        bos.write("Testing".getBytes(testMessageCharset));
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        bos.write("Testing".getBytes(testMessageCharset));
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, false);
        // Assert that the bytes returned from the stream handler are correct
        assertTrue(Arrays.equals(testBytes, streamHandler.getNextMessage()));

        class FailingByteArrayInputStream extends ByteArrayInputStream {
            private int maxRead;
            private int numRead;

            public FailingByteArrayInputStream(byte[] buf, int maxRead) {
                super(buf);
                this.maxRead = maxRead;
                numRead = 0;
            }

            @Override
            public int read() {
                if (++numRead > maxRead) {
                    throw new RuntimeException("fail");
                }
                return super.read();
            }
        }

        // Create regular LLP frame
        bos = new ByteArrayOutputStream();
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        // Create an input stream that will throw an exception
        maxRead = 50;
        is = new FailingByteArrayInputStream(bos.toByteArray(), maxRead);
        // Allow the stream handler to return data when an exception occurs
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, true);
        // Assert that the bytes returned from the stream handler are correct
        assertTrue(Arrays.equals(Arrays.copyOf(testBytes, maxRead - llpStartBytes.length), streamHandler.getNextMessage()));

        class FailingFilterInputStream extends FilterInputStream {
            private int maxRead;
            private int numRead;
            private boolean fail = true;

            public FailingFilterInputStream(InputStream in, int maxRead) {
                super(in);
                this.maxRead = maxRead;
                numRead = 0;
            }

            public void toggleFail() {
                fail = !fail;
            }

            @Override
            public int read() throws IOException {
                if (numRead >= maxRead && fail) {
                    throw new SocketTimeoutException("fail");
                }
                int result = super.read();
                numRead++;
                return result;
            }
        }

        // Create regular LLP frame
        bos = new ByteArrayOutputStream();
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        // Create an input stream that will throw an exception
        maxRead = 50;
        is = new FailingFilterInputStream(new ByteArrayInputStream(bos.toByteArray()), maxRead);
        // Allow the stream handler to return data when an exception occurs
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, true);
        // Assert that the bytes returned from the stream handler are correct
        assertTrue(Arrays.equals(Arrays.copyOf(testBytes, maxRead - llpStartBytes.length), streamHandler.getNextMessage()));

        // Create regular LLP frame
        bos = new ByteArrayOutputStream();
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        // Create an input stream that will throw an exception
        maxRead = 50;
        is = new FailingFilterInputStream(new ByteArrayInputStream(bos.toByteArray()), maxRead);
        // Allow the stream handler to return data when an exception occurs
        streamHandler = new StreamHandler(is, null, llpStartBytes, llpEndBytes, true);
        // Get the first set of bytes
        byte[] firstBytes = streamHandler.getNextMessage();
        // Turn failing off and get the rest of the bytes
        ((FailingFilterInputStream) is).toggleFail();
        byte[] nextBytes = streamHandler.getNextMessage();
        // Assert that the concatenation of both byte arrays is equivalent to the original message
        assertTrue(Arrays.equals(testBytes, ArrayUtils.addAll(firstBytes, nextBytes)));
    }

    @Test
    public void readDelimiterBatch() throws Exception {
        byte[] testBytes = testMessageBytes;
        byte[] delimiterBytes = new byte[] { 0x11, 0x12, 0x13, 0x14 };
        ByteArrayOutputStream bos;
        InputStream is;
        DelimiterBatchStreamHandler streamHandler;
        byte[] returnBytes;
        int numMessages;

        // Add two messages to the output stream
        bos = new ByteArrayOutputStream();
        bos.write(testBytes);
        bos.write(delimiterBytes);
        bos.write(testBytes);
        bos.write(delimiterBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new DelimiterBatchStreamHandler(is, delimiterBytes, false);
        // Assert that the bytes returned from the stream handler (twice) are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(2, numMessages);

        // Add two messages to the output stream, with a single beginning byte sequence
        bos = new ByteArrayOutputStream();
        bos.write("Testing".getBytes(testMessageCharset));
        bos.write(llpStartBytes);
        bos.write(testBytes);
        bos.write(delimiterBytes);
        bos.write(testBytes);
        bos.write(delimiterBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new DelimiterBatchStreamHandler(is, delimiterBytes, false, llpStartBytes, new byte[0]);
        // Assert that the bytes returned from the stream handler (twice) are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(2, numMessages);

        // Add three messages to the output stream, with a single ending byte sequence
        bos = new ByteArrayOutputStream();
        bos.write(testBytes);
        bos.write(delimiterBytes);
        bos.write(testBytes);
        bos.write(delimiterBytes);
        bos.write(testBytes);
        bos.write(llpEndBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new DelimiterBatchStreamHandler(is, delimiterBytes, false, new byte[0], llpEndBytes);
        // Assert that the bytes returned from the stream handler (twice) are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(3, numMessages);
    }

    @Test
    public void readRegexBatch() throws Exception {
        byte[] testBytes = testMessageBytes;
        String delimiterRegex = "foobar";
        String charsetEncoding = testMessageCharset;
        ByteArrayOutputStream bos;
        InputStream is;
        RegexBatchStreamHandler streamHandler;
        byte[] returnBytes;
        int numMessages;

        // Add two messages to the output stream
        bos = new ByteArrayOutputStream();
        bos.write(testBytes);
        bos.write(delimiterRegex.getBytes(charsetEncoding));
        bos.write(testBytes);
        bos.write(delimiterRegex.getBytes(charsetEncoding));
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new RegexBatchStreamHandler(is, delimiterRegex, charsetEncoding, false);
        // Assert that the bytes returned from the stream handler (twice) are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(2, numMessages);
    }

    @Test
    public void readER7Batch() throws Exception {
        //byte[] testBytes = testMessageBytes;
        byte[] testBytes = "MSH\r".getBytes(testMessageCharset);
        ByteArrayOutputStream bos;
        InputStream is;
        ER7BatchStreamHandler streamHandler;
        byte[] returnBytes;
        int numMessages;

        // Add multiple messages to the output stream
        bos = new ByteArrayOutputStream();
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write(testBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new ER7BatchStreamHandler(is);
        // Assert that the bytes returned from the stream handler are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(5, numMessages);

        // Add multiple messages to the output stream, with header/trailer segments
        bos = new ByteArrayOutputStream();
        bos.write("FHS|||\rBHS|||\n".getBytes(testMessageCharset));
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write("BTS|||\r\nBHS|||\n".getBytes(testMessageCharset));
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write("BTS|||\rFTS|||\r\n".getBytes(testMessageCharset));
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new ER7BatchStreamHandler(is);
        // Assert that the bytes returned from the stream handler are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(5, numMessages);

        // Add multiple messages to the output stream, with header/trailer segments, and an LLP frame
        bos = new ByteArrayOutputStream();
        bos.write(llpStartBytes);
        bos.write("FHS|||\rBHS|||\n".getBytes(testMessageCharset));
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write("BTS|||\r\nBHS|||\n".getBytes(testMessageCharset));
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write(testBytes);
        bos.write("BTS|||\rFTS|||\r\n".getBytes(testMessageCharset));
        bos.write(llpEndBytes);
        // Create an input stream from the bytes
        is = new ByteArrayInputStream(bos.toByteArray());
        streamHandler = new ER7BatchStreamHandler(is, llpStartBytes, llpEndBytes);
        // Assert that the bytes returned from the stream handler are correct
        numMessages = 0;
        while ((returnBytes = streamHandler.getNextMessage()) != null) {
            assertTrue(Arrays.equals(testBytes, returnBytes));
            numMessages++;
        }
        // Assert that two messages were returned
        assertEquals(5, numMessages);
    }
}
