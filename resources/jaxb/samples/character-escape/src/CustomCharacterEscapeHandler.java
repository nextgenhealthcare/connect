/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.IOException;
import java.io.Writer;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

/*
 * @(#)$Id: CustomCharacterEscapeHandler.java,v 1.1 2004/06/25 21:11:52 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

public class CustomCharacterEscapeHandler implements CharacterEscapeHandler {
    
    /**
     * Escape characters inside the buffer and send the output to the writer.
     * 
     * @exception IOException
     *    if something goes wrong, IOException can be thrown to stop the
     *    marshalling process.
     */
    public void escape( char[] buf, int start, int len, boolean isAttValue, Writer out ) throws IOException {
        
        for( int i=start; i<start+len; i++ ) {
            char ch = buf[i];
            
            // you are supposed to do the standard XML character escapes
            // like & ... &amp;   < ... &lt;  etc
            
            if( ch=='&' ) {
                out.write("&amp;");
                continue;
            }
            
            if( ch=='"' && isAttValue ) {
                // isAttValue is set to true when the marshaller is processing
                // attribute values. Inside attribute values, there are more
                // things you need to escape, usually.
                out.write("&quot;");
                continue;
            }
            if( ch=='\'' && isAttValue ) {
                out.write("&apos;");
                continue;
            }
            
            // you should handle other characters like < or >
            
            
            if( ch>0x7F ) {
                // escape everything above ASCII to &#xXXXX;
                out.write("&#x");
                out.write( Integer.toHexString(ch) );
                out.write(";");
                continue;
            }
            
            // otherwise print normally
            out.write(ch);
        }
    }
}