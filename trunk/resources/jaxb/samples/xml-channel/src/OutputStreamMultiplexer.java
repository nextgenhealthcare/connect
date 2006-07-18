/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Allows multiple streams to be sent through one {@link OutputStream}.
 * 
 * <h2>Background</h2>
 * <p>
 * XML 1.0 requires a conforming XML parser to read all the data
 * until it hits the end of the stream because a parser needs to
 * make sure that nothing (except comments and PIs) follows the end
 * tag of the document element.
 * 
 * This makes it difficult to reuse one stream to send multiple
 * XML documents.
 * 
 * <p>
 * This multiplexer allows you to split one {@link OutputStream}
 * to multiple sub {@link OutputStream} to overcome this problem
 * of XML 1.0, without sacrificing efficiency to much.
 * 
 * <p>
 * Although this class is primarily developed for XML, nothing
 * prevents you from sending other kinds of data over the stream.
 * 
 * 
 * <h2>Usage</h2>
 * <p>
 * To use this class, do as follows:
 * <pre>
 *   OutpuStream os = createTheStreamToSendData();
 *   OutputStreamMultiplexer osm = new OutputStreamMultiplexer(os);
 *   
 *   while( ... ) {
 *      OutputStream ss = osm.openSubStream();
 *      // send data to this stream
 *      ss.write(...);
 *      // it is important to close the stream
 *      ss.close();
 *   }
 *   
 *   osm.close();
 * </pre>
 * 
 * 
 * <h2>Note</h2>
 * <p>
 * You can only use at most one sub {@link OutputStream} at any
 * given timing. This class doesn't allow you to use two sub
 * {@link OutputStream}s at the same time.
 * 
 * <p>
 * Closing a sub-stream will automatically flush the data.
 * 
 * <p>
 * After you closed a sub-stream, you can leave the underlying
 * stream open and use it to send some more data directly to the
 * underlying stream. The {@link InputStreamDemultiplexer} can
 * still correcly de-multiplex the stream you sent, and you can
 * then start reading from the underlying input stream to read
 * data you sent directly through the underlying output stream.
 * 
 * <p>
 * This class should be used in pair with {@link InputStreamDemultiplexer}.
 * 
 * <p>
 * Because of the way the multiplexing work, flushing a sub stream
 * doesn't always flush all the pending data.
 * 
 * @see InputStreamDemultiplexer
 *      
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class OutputStreamMultiplexer {
    
    /** Data will be sent to this stream. */
    private final DataOutputStream underlyingStream;
    
    private SubStream subStream;
    
    private final byte[] buffer = new byte[512];
    private int bufPtr=0;
    
    /**
     * Creates a new instance.
     * 
     * @param underlyingStream
     *      The data will be ultimately sent to this stream.
     */
    OutputStreamMultiplexer( OutputStream underlyingStream ) {
        this.underlyingStream = new DataOutputStream(underlyingStream);  
    }
    
    /**
     * Opens a new sub-stream.
     * 
     * @exception IllegalStateException
     *      If the previous stream is still open. You cannot keep
     *      two sub-streams open at the same time.
     * @return
     *      Always return non-null valid object.
     */
    public OutputStream openSubStream() {
        if( subStream!=null )
            throw new IllegalStateException("the previous sub-stream is still open");
        
        subStream = new SubStream();
        return subStream;
    }
    
    /**
     * Closes the underlying stream by calling its <code>close</code> method.
     * 
     * @exception IllegalStateException
     *      If a sub-stream is still open. A sub-stream has to be closed
     *      before the underlying output stream is closed.
     */
    public void close() throws IOException {
        if( subStream!=null )
            throw new IllegalStateException("the previous sub-stream is still open");
        underlyingStream.close();
    }
    
    private class SubStream extends OutputStream
    {
        public void close() throws IOException {
            if(subStream!=this)     return; // already closed
            
            // tell the parent that we are done.
            subStream = null;
            
            // send the last packet
            underlyingStream.writeShort(0x8000/*TAIL indicator*/|bufPtr);
            underlyingStream.write( buffer, 0, bufPtr );
            underlyingStream.flush();
            
            // reinitialize the buffer
            bufPtr = 0;
        }
        
        public void flush() throws IOException {
            if(subStream!=this) throw new IOException("stream is already closed");
            underlyingStream.flush();
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if(subStream!=this) throw new IOException("stream is already closed");
            
            while( len!=0 ) {
                dispatch();
                
                // fast transfer that doesn't go through our buffer
                if( bufPtr==0 ) {
                    while( len >= buffer.length ) {
                        underlyingStream.writeShort(buffer.length);
                        underlyingStream.write( b, off, buffer.length );
                        off += buffer.length;
                        len -= buffer.length;
                    }
                }
            
                int size = Math.min( buffer.length-bufPtr, len );
                System.arraycopy(b,off,buffer,bufPtr,size);
                bufPtr += size;
                off += size;
                len -= size;
            }
        }

        public void write(int b) throws IOException {
            if(subStream!=this) throw new IOException("stream is already closed");
            
            dispatch();
            buffer[bufPtr++] = (byte)b;
        }

        /**
         * Sends a new packet to the other end if the buffer is full.
         */
        private void dispatch() throws IOException {
            if( bufPtr==buffer.length ) {
                underlyingStream.writeShort(buffer.length);
                underlyingStream.write( buffer );
                bufPtr = 0;
            }
        }
    }
}
