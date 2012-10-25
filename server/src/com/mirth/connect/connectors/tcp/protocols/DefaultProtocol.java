/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.tcp.protocols;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.connectors.tcp.TcpConnector;
import com.mirth.connect.connectors.tcp.TcpProtocol;

/**
 * The DefaultProtocol class is an application level tcp protocol that does
 * nothing. Reading is performed in reading the socket until no more bytes are
 * available. Writing simply writes the data to the socket.
 * 
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.4 $
 */
public class DefaultProtocol implements TcpProtocol {

	private static final int BUFFER_SIZE = 8192;

	private static final Log logger = LogFactory.getLog(DefaultProtocol.class);
	
	private TcpConnector _tcpConnector;
	
	public void setTcpConnector(TcpConnector tcpConnector) {
		_tcpConnector = tcpConnector;
	}
	
	public byte[] read(InputStream is) throws IOException {
		String charset=_tcpConnector.getCharsetEncoding();
		UtilReader myReader = new UtilReader(is, charset);

		int c = 0;
		try {
			c = myReader.read();
		} catch (SocketException e) {
			logger.info("SocketException on read() attempt.  Socket appears to have been closed: " + e.getMessage());
			// Throw the exception so the socket can always be recycled.
			throw e;
		} catch (SocketTimeoutException ste) {
			logger.info("SocketTimeoutException on read() attempt.  Socket appears to have been closed: " + ste.getMessage());
            /*
             * Throw the exception so the listener can know it was a timeout and
             * decide whether or not to recycle the connection.
             */
			throw ste;
		}

		// trying to read when there is no data (stream may have been closed at
		// other end)
		if (c == -1) {
			logger.info("End of input stream reached.");
			return null;
		}

		while (c != -1) {
			myReader.append((char) c);
			try {
				c = myReader.read();
			} catch (Exception e) {
				c = -1;
			}
		}

		return myReader.getBytes();
	}

	public void write(OutputStream os, byte[] data) throws IOException {
		os.write(data);
	}
	
	// ast: a class to read both types of stream; bytes ( char limits ) and
	// chars (byte limits)
	protected class UtilReader {
		InputStream byteReader=null;
		ByteArrayOutputStream baos = null;
		String charset="";		

		public UtilReader(InputStream is, String charset) {
			this.charset=charset;
			this.byteReader=is;			
			baos = new ByteArrayOutputStream();
		}
	
		public int read() throws java.io.IOException {
			if (byteReader!=null) return byteReader.read();
			else return -1;
		}

		public void close() throws java.io.IOException {
			if (byteReader!=null) 	byteReader.close();

		}
		public void append(int c) throws java.io.IOException {
			baos.write(c);			
		}
		public String toString() {	
            try{
                baos.flush();
                baos.close();
            }catch(Throwable t){
                logger.error("Error closing the auxiliar buffer "+t);
            }
			try{
				return new String(baos.toByteArray(),this.charset);
			}catch(java.io.UnsupportedEncodingException e){
				logger.error("Error: "+charset+" is unsupported, changing to default encoding");
				return new String(baos.toByteArray());
			}
		}

		public byte[] getBytes() throws java.io.IOException {
			baos.flush();
			baos.close();
			return baos.toByteArray();
		}
		public void reset(){
			baos.reset();
		}
	}
}
