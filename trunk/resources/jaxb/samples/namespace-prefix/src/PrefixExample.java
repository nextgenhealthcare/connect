/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;


/**
 * This example shows you how you can change the way
 * the marshaller assigns prefixes to namespace URIs.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PrefixExample {
    
    public static void main( String[] args ) throws Exception {
        // in this example, I skip the error check entirely
        // for the sake of simplicity. In reality, you should
        // do a better job of handling errors.
        for( int i=0; i<args.length; i++ )
            test(args[i]);      // run through all the files one by one.
    }
    
    private static void test( String fileName ) throws Exception {
        JAXBContext context = JAXBContext.newInstance("foo:bar"); 
        
        // unmarshal a file specified by the command line argument
        Object o = context.createUnmarshaller().unmarshal(new File(fileName));
        
        Marshaller marshaller = context.createMarshaller();
        
        // to specify the URI->prefix mapping, you'll need to provide an
        // implementation of NamespaecPrefixMapper, which determines the
        // prefixes used for marshalling.
        // 
        // you specify this as a property of Marshaller to
        // tell the marshaller to consult your mapper
        // to assign a prefix for a namespace.
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NamespacePrefixMapperImpl());
        } catch( PropertyException e ) {
            // if the JAXB provider doesn't recognize the prefix mapper,
            // it will throw this exception. Since being unable to specify
            // a human friendly prefix is not really a fatal problem,
            // you can just continue marshalling without failing
            ;
        }
        
        // make the output indented. It looks nicer on screen.
        // this is a JAXB standard property, so it should work with any JAXB impl.
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        
        // print it out to the console since we are just testing the behavior.
        marshaller.marshal( o, System.out );
    }
}
