/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
import java.util.Enumeration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

import primer.PurchaseOrder;

/*
 * @(#)$Id: Splitter.java,v 1.1 2004/06/25 21:12:17 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
 
/**
 * This object implements XMLFilter and monitors the incoming SAX
 * events. Once it hits a purchaseOrder element, it creates a new
 * unmarshaller and unmarshals one purchase order.
 * 
 * <p>
 * Once finished unmarshalling it, we will process it, then move
 * on to the next purchase order.
 */
public class Splitter extends XMLFilterImpl {
    
    public Splitter( JAXBContext context ) {
        this.context = context;
    }
    
    /**
     * We will create unmarshallers from this context.
     */
    private final JAXBContext context;
    
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
            
        if( depth!= 0 ) {
            // we are in the middle of forwarding events.
            // continue to do so.
            depth++;
            super.startElement(namespaceURI, localName, qName, atts);
            return;
        }
        
        if( namespaceURI.equals("") && localName.equals("purchaseOrder") ) {
            // start a new unmarshaller
            Unmarshaller unmarshaller;
            try {
                unmarshaller = context.createUnmarshaller();
            } catch( JAXBException e ) {
                // there's no way to recover from this error.
                // we will abort the processing.
                throw new SAXException(e);
            }
            unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
            
            // set it as the content handler so that it will receive
            // SAX events from now on.
            setContentHandler(unmarshallerHandler);
            
            // fire SAX events to emulate the start of a new document.
            unmarshallerHandler.startDocument();
            unmarshallerHandler.setDocumentLocator(locator);
            
            Enumeration e = namespaces.getPrefixes();
            while( e.hasMoreElements() ) {
                String prefix = (String)e.nextElement();
                String uri = namespaces.getURI(prefix);
                
                unmarshallerHandler.startPrefixMapping(prefix,uri);
            }
            String defaultURI = namespaces.getURI("");
            if( defaultURI!=null )
                unmarshallerHandler.startPrefixMapping("",defaultURI);

            super.startElement(namespaceURI, localName, qName, atts);
            
            // count the depth of elements and we will know when to stop.
            depth=1;
        }
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        
        // forward this event
        super.endElement(namespaceURI, localName, qName);
        
        if( depth!=0 ) {
            depth--;
            if( depth==0 ) {
                // just finished sending one chunk.
                
                // emulate the end of a document.
                Enumeration e = namespaces.getPrefixes();
                while( e.hasMoreElements() ) {
                    String prefix = (String)e.nextElement();
                    unmarshallerHandler.endPrefixMapping(prefix);
                }
                String defaultURI = namespaces.getURI("");
                if( defaultURI!=null )
                    unmarshallerHandler.endPrefixMapping("");
                unmarshallerHandler.endDocument();
                
                // stop forwarding events by setting a dummy handler.
                // XMLFilter doesn't accept null, so we have to give it something,
                // hence a DefaultHandler, which does nothing.
                setContentHandler(new DefaultHandler());
                
                // then retrieve the fully unmarshalled object
                try {
                    PurchaseOrder result = (PurchaseOrder)unmarshallerHandler.getResult();
                    
                    // process this new purchase order
                    process(result);
                } catch( JAXBException je ) {
                    // error was found during the unmarshalling.
                    // you can either abort the processing by throwing a SAXException,
                    // or you can continue processing by returning from this method.
                    System.err.println("unable to process an order at line "+
                        locator.getLineNumber() );
                    return;
                }
                
                unmarshallerHandler = null;
            }
        }
    }
    
    public void process( PurchaseOrder order ) {
        System.out.println("this order will be shipped to "
            + order.getShipTo().getName() );
    }
    
    /**
     * Remembers the depth of the elements as we forward
     * SAX events to a JAXB unmarshaller.
     */
    private int depth;
    
    /**
     * Reference to the unmarshaller which is unmarshalling
     * an object.
     */
    private UnmarshallerHandler unmarshallerHandler;


    /**
     * Keeps a reference to the locator object so that we can later
     * pass it to a JAXB unmarshaller.
     */
    private Locator locator;
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }


    /**
     * Used to keep track of in-scope namespace bindings.
     * 
     * For JAXB unmarshaller to correctly unmarshal documents, it needs
     * to know all the effective namespace declarations.
     */
    private NamespaceSupport namespaces = new NamespaceSupport();
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaces.pushContext();
        namespaces.declarePrefix(prefix,uri);
        
        super.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        namespaces.popContext();
        
        super.endPrefixMapping(prefix);
    }
}
