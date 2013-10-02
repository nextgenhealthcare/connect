/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class CmdUtil {
	
    public static int execCmd(String[] cmdLine, boolean waitFor) throws Exception {
    	String[] cmd = concat(ServiceControllerFactory.getServiceController().getCommand().split(" "), cmdLine);
        Process process = Runtime.getRuntime().exec(cmd);

        if (!waitFor) {
            return 0;
        }
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);

        outPumper.start();
        errPumper.start();
        process.waitFor();
        outPumper.join();
        errPumper.join();

        return process.exitValue();
    }
    
    public static int execCmd(String cmdLine, boolean waitFor) throws Exception {
        return execCmd(cmdLine.split(" "), waitFor);
    }

    public static String execCmdWithOutput(String[] cmdLine) throws Exception {
    	String[] cmd = concat(ServiceControllerFactory.getServiceController().getCommand().split(" "), cmdLine);
        Process process = Runtime.getRuntime().exec(cmd);
        
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);

        outPumper.start();
        errPumper.start();
        process.waitFor();
        outPumper.join();
        errPumper.join();

        return outPumper.getOutput();
    }
    
    public static String execCmdWithOutput(String cmdLine) throws Exception {
    	return execCmdWithOutput(cmdLine.split(" "));
    }

    public static String execCmdWithErrorOutput(String[] cmdLine) throws Exception {
    	String[] cmd = concat(ServiceControllerFactory.getServiceController().getCommand().split(" "), cmdLine);
        Process process = Runtime.getRuntime().exec(cmd);
        
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);

        outPumper.start();
        errPumper.start();
        process.waitFor();
        outPumper.join();
        errPumper.join();

        return errPumper.getOutput();
    }
    
    public static String execCmdWithErrorOutput(String cmdLine) throws Exception {
    	return execCmdWithErrorOutput(cmdLine.split(" "));
    }
    
    private static String[] concat(String[] a, String[] b) {
    	String[] c = new String[a.length + b.length];
    	
    	int i = 0;
    	
    	for (int j = 0; j < a.length; j++) {
    		c[i] = a[j];
    		i++;
    	}
    	
    	for (int j = 0; j < b.length; j++) {
    		c[i] = b[j];
    		i++;
    	}
    	
    	return c;
    }

    private static class StreamPumper extends Thread {

        private InputStream is;
        private PrintStream os;
        private StringBuffer output;

        public StreamPumper(InputStream is, PrintStream os) {
            this.is = is;
            this.os = os;

            output = new StringBuffer();
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = br.readLine()) != null) {
                    output.append(line + "\n");
                    os.println(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getOutput() {
            return output.toString();
        }
    }
}
