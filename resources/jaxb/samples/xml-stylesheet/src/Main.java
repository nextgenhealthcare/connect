/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import primer.PurchaseOrder;

/*
 * @(#)$Id: Main.java,v 1.1 2004/06/25 21:12:50 kohsuke Exp $
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
        
        // unmarshal a document, just to marshal it back again.
        PurchaseOrder po = (PurchaseOrder)context.createUnmarshaller().unmarshal(
            new File(args[0]));
        // we don't need to check the return value, because the unmarshal
        // method should haven thrown an exception if anything went wrong.
        
        
        // Here's the real meat.
        // we configure marshaller not to print out xml decl,
        // we then print out XML decl plus stylesheet header on our own,
        // then have the marshaller print the real meat.
        
        System.out.println("<?xml version='1.0'?>");
        System.out.println("<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>");
        // if you need to put DOCTYPE decl, it can be easily done here.
        
        // create JAXB marshaller.
        Marshaller marshaller = context.createMarshaller();
        // configure it
        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.FALSE);
        // marshal
        marshaller.marshal(po,System.out);
    }

}
