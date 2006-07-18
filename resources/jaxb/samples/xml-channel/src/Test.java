/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;


/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Test {

    public static Object lock = new Object();
    public static boolean ready = false;
    
    public static void main(String[] args) throws Exception {
        // launch the server
        new Thread(new TestServer()).start();

        // wait for the server to become ready
        while( !ready ) {
            synchronized( lock ) {
                lock.wait(1000);
            }
        }
        
        // reset the flag
        ready = false;
        
        // run the client
        new TestClient().run();

        // wait for the server to finish processing data  
        // from the client 
        while( !ready ) {
            synchronized( lock ) {
                lock.wait(1000);
            }
        }

        System.exit(0);
    }
}
