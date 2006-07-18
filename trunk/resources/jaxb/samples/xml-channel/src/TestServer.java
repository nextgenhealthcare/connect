/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import message.*;

/**
 * Server program that displays the messages sent from clients.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TestServer implements Runnable {

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(38247);
            JAXBContext context = JAXBContext.newInstance("message");

            // notify test driver that we are ready to accept
            synchronized( Test.lock ) {
                Test.ready = true;
                Test.lock.notifyAll();
            }
            
            while(true) {
                new Worker(ss.accept(),context).start();
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    static class Worker extends Thread {
        private final InputStreamDemultiplexer isd;
        private final Unmarshaller unmarshaller;
        
        Worker( Socket socket, JAXBContext context ) throws IOException, JAXBException {
            System.out.println("accepted connection from client");
            this.isd = new InputStreamDemultiplexer(socket.getInputStream());
            this.unmarshaller = context.createUnmarshaller();
            unmarshaller.setValidating(false);
        }
        
        public void run() {
            try {
                InputStream channel;
                while( (channel=isd.openNextStream())!=null ) {
                    // unmarshal a new object
                    Message msg = (Message)unmarshaller.unmarshal(channel);
                    System.out.println("Message: "+msg.getValue());
                    channel.close();
                }
                System.out.println("Bye!");
                isd.close();

                // notify the driver that we are done processing
                synchronized( Test.lock ) {
                    Test.ready = true;
                    Test.lock.notifyAll();
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
