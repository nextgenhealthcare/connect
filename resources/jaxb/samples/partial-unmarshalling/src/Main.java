/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

/*
 * @(#)$Id: Main.java,v 1.1 2004/06/25 21:12:17 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

public class Main {
    public static void main( String[] args ) throws Exception {
        
        // create JAXBContext for the primer.xsd
        JAXBContext context = JAXBContext.newInstance("primer");
        
        // create a new XML parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XMLReader reader = factory.newSAXParser().getXMLReader();
        
        // prepare a Splitter
        Splitter splitter = new Splitter(context);
        
        // connect two components
        reader.setContentHandler(splitter);
        
        for( int i=0; i<args.length; i++ ) {
            // parse all the documents specified via the command line.
            // note that XMLReader expects an URL, not a file name.
            // so we need conversion.
            reader.parse(new File(args[i]).toURL().toExternalForm());
        }
    }

}
