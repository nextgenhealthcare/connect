package org.mule.providers.llprouter.protocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.providers.llprouter.TcpProtocol;;

/**
 * The LLP Protocol is suitable for sending and receiving HL7 formatted messages.
 * Adapted from HAPI HL7 Reader:
 * http://cvs.sourceforge.net/viewcvs.py/hl7api/hapi/ca/uhn/hl7v2/llp/MinLLPReader.java?rev=1.9&view=auto
 * @author <a href="mailto:eclipxe@gmail.com">Chris Lang</a>
 * @version $Revision: 1.0 $
 */
public class LlpProtocol implements TcpProtocol
{
	private static final Log logger = LogFactory.getLog(LlpProtocol.class);
	public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";

    private static final char END_MESSAGE = '\u001c'; //character indicating the
                                              //termination of an HL7 message
    private static final char START_MESSAGE = '\u000b';//character indicating the
                                               //start of an HL7 message
    private static final char LAST_CHARACTER = 13; //the final character of
        //a message: a carriage return
    
    /* Reads an inputstream and parses messages based on HL7 delimiters
     * @param is	The InputStream to read from
     * @return		<code>byte[]</code> containing one HL7 message. 
     * 				<code>null</code> if the incoming message does not contain HL7 
     * 				message delimiters
     */
    public synchronized byte[] read(InputStream is) throws IOException
    {
    	BufferedReader myReader;
    	String charset = System.getProperty(CHARSET_KEY, "US-ASCII");
    	
        if (charset.equals("default")) {
            myReader = new BufferedReader(new InputStreamReader(is));
        } else {
            myReader = new BufferedReader(new InputStreamReader(is, charset));
        }
        
        StringBuffer s_buffer = new StringBuffer();

        boolean end_of_message = false;
                        
            int c = 0;
            try {
                c = myReader.read();                
            } catch (SocketException e) {
                logger.info("SocketException on read() attempt.  Socket appears to have been closed: " + e.getMessage());
                return null;
            } catch (SocketTimeoutException ste){
            	 logger.info("SocketTimeoutException on read() attempt.  Socket appears to have been closed: " + ste.getMessage());
                 return null;
            }

            //trying to read when there is no data (stream may have been closed at other end)
            if (c == -1) {
                logger.info("End of input stream reached.");
                return null; 
            }
         

            if (c != START_MESSAGE)
            {
            	while (c != -1){
            		s_buffer.append((char)c);
            		try{
            			c = myReader.read();
            		}catch (Exception e){
            			c = 1;
            		}
            	}
            	String message = s_buffer.toString();
            	logger.debug(message);
                throw new IOException("Message violates the "+
                    "minimal lower layer protocol: no start of message indicator "+
                    "received.");
            }

            while (! end_of_message)
            {
                c = myReader.read();

                if (c == -1)
                {
                    throw new IOException("Message violates the "+
                        "minimal lower protocol: message terminated without "+
                        "a terminating character.");
                }
                
                
                if (c == END_MESSAGE)
                {
                    //subsequent character should be a carriage return
                    try {
                        c = myReader.read();       
                        if (c >= 0) {
                        	
                        }
                        if (c != LAST_CHARACTER)
                        {
                            throw new IOException("Message "+
                                "violates the minimal lower layer protocol: "+
                                "message terminator not followed by a return "+
                                "character.");
                        }
                    } catch (SocketException e) {
                        logger.info("SocketException on read() attempt.  Socket appears to have been closed: " + e.getMessage());
                    } catch (SocketTimeoutException ste){
                    	 logger.info("SocketTimeoutException on read() attempt.  Socket appears to have been closed: " + ste.getMessage());
                    }
                    
                    end_of_message = true;
                }
                else
                {
                //the character wasn't the end of message, append it to the message
                    s_buffer.append((char) c);
                }
            } //end while

        return s_buffer.toString().getBytes();
    }

    public void write(OutputStream os, byte[] data) throws IOException
    {
        // Write the data with LLP wrappers
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeByte(START_MESSAGE);
        dos.write(data);
        dos.writeByte(END_MESSAGE);
        dos.writeByte(LAST_CHARACTER);
        dos.flush();
    }

}
