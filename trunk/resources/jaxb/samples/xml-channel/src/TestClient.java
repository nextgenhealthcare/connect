/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import message.*;

/**
 * Test client.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TestClient implements Runnable {

    private ObjectFactory of;
    private Marshaller marshaller;

    public TestClient() {
        try {
            of = new ObjectFactory();
            marshaller = of.createMarshaller();
        } catch( JAXBException e ) {
            e.printStackTrace(); // impossible
        }
    }
    
    public void run() {
        try {
            // create a socket connection and multiplex it
            Socket socket = new Socket("localhost",38247);
            OutputStreamMultiplexer osm = new OutputStreamMultiplexer(socket.getOutputStream());
            
            sendMessage(osm,"1st message");
            sendMessage(osm,"2nd message");
            
            osm.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private void sendMessage( OutputStreamMultiplexer osm, String msg ) throws JAXBException, IOException {
        Message m = of.createMessage();
        m.setValue(msg);
        
        OutputStream sub = osm.openSubStream();
        marshaller.marshal(m,sub);
        sub.close();
    }
}
