/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * De-multiplex one {@link InputStream} to multiple
 * sub-{@link InputStream}s. This class should be used
 * in pair with {@link OutputStreamMultiplexer}.
 * 
 * <p>
 * To use this class, do as follows:
 * <pre>
 * InputStream is = getInputStreamThatReceiveDataFromOutputStreamMultiplexer();
 * InputStreamDemultiplexer isd = new InputStreamDemultiplexer(is);
 * 
 * InputStream subStream;
 * while( (subStream=isd.openNextStream())!=null ) {
 *   // read from this sub-stream
 *   subStream.read(...);
 *   
 *   // it's important to close a sub-stream
 *   subStream.close();
 * }
 * isd.close();
 * </pre>
 * 
 * <p>
 * Note that because of the way the multiplexing works, the close method
 * of the sub-stream may block when you close a sub-stream before it
 * reaches the end-of-stream.
 * 
 * 
 * @see OutputStreamMultiplexer
 *      
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class InputStreamDemultiplexer {
    /** Data will be sent to this object. */
    private final DataInputStream underlyingStream;
    
    /** Currently active sub stream. */
    private SubStream subStream;
    
    private final byte[] buffer = new byte[512];
    private int bufPtr = 0;
    private int dataLength = 0;
    private boolean lastBlock = false;
    
    /**
     * Creates a new instance.
     * 
     * @param underlyingStream
     *      All the data will be read from this stream.
     */
    public InputStreamDemultiplexer( InputStream underlyingStream ) {
        this.underlyingStream = new DataInputStream(underlyingStream);
    }
    
    /**
     * Waits for the sender to send a next stream, then return it.
     * 
     * @return
     *      null if the sender closes the underlying stream. Otherwise
     *      non-null valid object.
     * 
     * @exception IOException
     *      If other unexpected errors happen.
     * 
     * @return IllegalStateException
     *      If a sub-stream is still open.
     */
    public InputStream openNextStream() throws IOException {
        if(subStream!=null)
            throw new IllegalStateException("previous sub-stream is still open");
        try {
            subStream = new SubStream();
            return subStream;
        } catch( EOFException e ) {
            return null;
        }
    }
    
    /**
     * Closes the underlying input stream.
     * 
     * @return IllegalStateException
     *      If a sub-stream is still open.
     */
    public void close() throws IOException {
        if(subStream!=null)
            throw new IllegalStateException("previous sub-stream is still open");
        underlyingStream.close();
    }
    
    private class SubStream extends InputStream
    {
        private SubStream() throws IOException {
            readNextBlock();
        }
        
        public void close() throws IOException {
            if( subStream!=this )
                return;     // this stream is already closed. ignore.
            
            // discard all the data till EoS
            while(!lastBlock)
                // read the next block until we hit the last block
                readNextBlock();
            
            // tell the parent that we are done.
            subStream = null;
            bufPtr = 0;
            dataLength = 0;
            lastBlock = false;
        }
        
        private void readNextBlock() throws IOException {
            short header = underlyingStream.readShort();
            lastBlock = (header&0x8000)!=0;
            dataLength = (header&0x7FFF);
            
            // read "dataLength" bytes
            underlyingStream.readFully(buffer,0,dataLength);
            
            bufPtr = 0;
        }
        
        public int read() throws IOException {
            if( subStream!=this )
                throw new IOException("trying to read from a closed stream");
            
            while(true) {
                if( bufPtr!=dataLength )
                    return buffer[bufPtr++];
                
                if( lastBlock )
                    return -1;  // EoS
                
                // always bufPtr==dataLength
                readNextBlock();
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if( subStream!=this )
                throw new IOException("trying to read from a closed stream");
            
            final int originalLen = len;
            
            while(len>0) {
                if( bufPtr!=dataLength ) {
                    // read from our buffer
                    int size = Math.min( len, dataLength-bufPtr );
                    System.arraycopy(buffer,bufPtr,b,off,size);
                    off += size;
                    len -= size;
                    bufPtr += size;
                }
                
                if( bufPtr==dataLength ) {
                    if( lastBlock ) { // EoS
                        if( len==originalLen )      return -1;  // no bytes read
                        else                        return originalLen-len;
                    }
                    
                    readNextBlock();
                }
            }
            
            return originalLen-len;
        }

        public int available() throws IOException {
            return dataLength-bufPtr;
        }
    }
}
