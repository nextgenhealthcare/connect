import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DTDSample {

    public static void main(String[] args) throws Exception {
        // in this example, I skip the error check entirely
        // for the sake of simplicity. In reality, you should
        // do a better job of handling errors.
        for( int i=0; i<args.length; i++ ) {
            test(args[i]);
        }
    }
    
    private static void test( String fileName ) throws Exception {
        
        // there's really nothing special about the code generated
        // from RELAX NG. So I'll just do the basic operation
        // to show that it actually feels exactly the same no matter
        // what schema language you use.
        
        JAXBContext context = JAXBContext.newInstance("foo.jaxb");
        
        // unmarshal a file. Just like you've always been doing.
        Object o = context.createUnmarshaller().unmarshal(new File(fileName)); 
        
        // valdiate it. Again, the same procedure regardless of the schema language
        context.createValidator().validate(o);
        
        // marshal it. Nothing new.
        Marshaller m = context.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.marshal(o,System.out);
    }
}
