package com.webreach.mirth.server.mule.providers.mllp.protocols;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.server.mule.providers.mllp.MllpConnector;
import com.webreach.mirth.server.mule.providers.mllp.TcpProtocol;

/**
 * The LLP Protocol is suitable for sending and receiving HL7 formatted
 * messages. Adapted from HAPI HL7 Reader:
 * http://cvs.sourceforge.net/viewcvs.py/hl7api/hapi/ca/uhn/hl7v2/llp/MinLLPReader.java?rev=1.9&view=auto
 * 
 * @author <a href="mailto:eclipxe@gmail.com">Chris Lang</a>
 * @version $Revision: 1.0 $
 */
public class LlpProtocol implements TcpProtocol {
	private static final Log logger = LogFactory.getLog(LlpProtocol.class);

	// ast: buffer size for byte read
	private static final int BUFFER_SIZE = 8192;

	private char END_MESSAGE = 0x1C; // character indicating end of message
	private char START_MESSAGE = 0x0B; // first character of a new message
	private char END_OF_RECORD = 0x0D; // character sent between messages
	private char END_OF_SEGMENT = 0x0D; // character sent between hl7 segments
	// (usually same as end of record)
	private boolean useLLP = true;
	private MllpConnector _mllpConnector;

	public void setTcpConnector(MllpConnector mllpConnector) {
		try {
			_mllpConnector = mllpConnector;
			if (_mllpConnector.getCharEncoding().equals("hex")) {
				START_MESSAGE = (char) Integer.decode(_mllpConnector.getMessageStart()).intValue();
				END_MESSAGE = (char) Integer.decode(_mllpConnector.getMessageEnd()).intValue();
				END_OF_RECORD = (char) Integer.decode(_mllpConnector.getRecordSeparator()).intValue();
				END_OF_SEGMENT = (char) Integer.decode(_mllpConnector.getSegmentEnd()).intValue();

			} else {
				START_MESSAGE = _mllpConnector.getMessageStart().charAt(0);
				END_MESSAGE = _mllpConnector.getMessageEnd().charAt(0);
				END_OF_RECORD = _mllpConnector.getRecordSeparator().charAt(0);
				END_OF_SEGMENT = _mllpConnector.getSegmentEnd().charAt(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] readTCP(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		int len = 0;
		try {
			while ((len = is.read(buffer)) == 0) {
			}
		} catch (SocketException e) {
			// do not pollute the log with a stacktrace, log only the message
			logger.debug("Socket exception occured: " + e.getMessage());
			return null;
		} catch (SocketTimeoutException e) {
			logger.debug("Socket timeout, returning null.");
			return null;
		}
		try {
			if (len == -1) {
				return null;
			} else {
				do {
					baos.write(buffer, 0, len);
					if (len < buffer.length) {
						break;
					}
					int av = is.available();
					if (av == 0) {
						break;
					}
				} while ((len = is.read(buffer)) > 0);

				baos.flush();
				baos.close();
				return baos.toByteArray();
			}
		} finally {
			baos.flush();
			baos.close();
		}
	}
	
	/*
	 * Reads an inputstream and parses messages based on HL7 delimiters @param
	 * is The InputStream to read from @return <code>byte[]</code> containing
	 * one HL7 message. <code>null</code> if the incoming message does not
	 * contain HL7 message delimiters
	 */
	private byte[] readLLP(InputStream is) throws IOException {

		// ast: new function to process the MLLP protocol
		// ast: wrapper for the reader
		UtilReader myReader = null;
		if (_mllpConnector.getCharEncoding().equals("hex")) {
			myReader = new UtilReader(new BufferedInputStream(is, BUFFER_SIZE));
		} else {
			String charset = _mllpConnector.getCharsetEncoding();
			myReader = new UtilReader(is, charset);
		}
		
		boolean end_of_message = false;

		int c = 0;
		try {
			c = myReader.read();
		} catch (SocketException e) {
			logger.info("SocketException on read() attempt.  Socket appears to have been closed: " + e.getMessage());
			return null;
		} catch (SocketTimeoutException ste) {
			logger.info("SocketTimeoutException on read() attempt.  Socket appears to have been closed: " + ste.getMessage());
			return null;
		}

		// trying to read when there is no data (stream may have been closed at
		// other end)
		if (c == -1) {
			logger.info("End of input stream reached.");
			return null;
		}

		if (c != START_MESSAGE) {
			while (c != -1) {
				myReader.append((char) c);
				try {
					c = myReader.read();
				} catch (Exception e) {
					c = 1;
				}
			}
			String message = myReader.toString();
			logger.debug(message);
			throw new IOException("Message violates the " + "minimal lower layer protocol: no start of message indicator " + "received.");
		}else{
			myReader.append((char) c);
		}
		while (!end_of_message) {
			c = myReader.read();

			if (c == -1) {
				throw new IOException("Message violates the " + "minimal lower protocol: message terminated without " + "a terminating character.");
			}
			myReader.append((char) c);
			if (c == END_MESSAGE) {

				if (END_OF_RECORD != 0) {
					// subsequent character should be a carriage return
					try {
						c = myReader.read();
						if (c >= 0) {

						}
						if (END_OF_RECORD != 0 && c != END_OF_RECORD) {
							logger.error("Message terminator was: " + c + "  Expected terminator: " + END_OF_RECORD);
							throw new IOException("Message " + "violates the minimal lower layer protocol: " + "message terminator not followed by a return " + "character." + myReader.toString());
						}
						myReader.append((char) c);
					} catch (SocketException e) {
						logger.info("SocketException on read() attempt.  Socket appears to have been closed: " + e.getMessage());
					} catch (SocketTimeoutException ste) {
						logger.info("SocketTimeoutException on read() attempt.  Socket appears to have been closed: " + ste.getMessage());
					}
				}
				end_of_message = true;
				
			} else {
				// the character wasn't the end of message, append it to the
				// message
				//myReader.append((char) c);
			}
		} // end while

		return myReader.getBytes();
	}

	
	public byte[] read(InputStream is) throws IOException {
		if (useLLP) {
			return readLLP(is);
		} else {
			return readTCP(is);
		}
	}

	public void write(OutputStream os, byte[] data) throws IOException {
		// Write the data with LLP wrappers
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeByte(START_MESSAGE);
		dos.write(data);
		dos.writeByte(END_MESSAGE);
		if (END_OF_RECORD != 0) {
			dos.writeByte(END_OF_RECORD);
		}
		try {
			dos.flush();
		} catch (SocketException se) {
			logger.debug("Socket closed while trying to flush");
		}
	}

	// ast: a class to read both types of stream; bytes ( char limits ) and
	// chars (byte limits)
	protected class UtilReader {
		BufferedReader charReader = null;
		BufferedInputStream byteReader = null;
		ByteArrayOutputStream baos = null;
		String charset = "UTF-8";
		char[] theChar = new char[1];

		public UtilReader(InputStream bf, String charset) {
			try {
				this.charReader = new BufferedReader(new InputStreamReader(bf, charset));
			} catch (java.io.UnsupportedEncodingException t) {
				this.charReader = new BufferedReader(new InputStreamReader(bf));
			}
			this.charset = charset;
			this.byteReader = null;
			baos = new ByteArrayOutputStream();
		}

		public UtilReader(BufferedInputStream bi) {
			this.charReader = null;
			this.byteReader = bi;
			baos = new ByteArrayOutputStream();
		}

		public int read() throws java.io.IOException {
			if (charReader != null)
				return charReader.read();
			else if (byteReader != null)
				return byteReader.read();
			else
				return -1;
		}

		public void close() throws java.io.IOException {
			if (charReader != null)
				charReader.close();
			else if (byteReader != null)
				byteReader.close();

		}

		public void append(int c) throws java.io.IOException {
			if (charReader != null) {
				theChar[0] = (char) c;
				String s = new String(theChar);
				baos.write(s.getBytes());
			} else {
				baos.write(c);
			}
		}

		public String toString() {
			try {
				baos.flush();
				baos.close();
			} catch (Throwable t) {
				logger.error("Error closing the auxiliar buffer " + t);
			}
			return new String(baos.toByteArray());
		}

		public byte[] getBytes() throws java.io.IOException {
			baos.flush();
			baos.close();
			return baos.toByteArray();
		}

	}

	public boolean isUseLLP() {
		return useLLP;
	}

	public void setUseLLP(boolean useLLP) {
		this.useLLP = useLLP;
	}
}
