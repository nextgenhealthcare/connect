package com.mirth.connect.simplereceiver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleReceiver {
    protected int count = 0;
    protected long startTime;
    protected int lastCount = 0;
    protected long lastTime;

    private char END_MESSAGE = 0x1C; // character indicating end of message
    private char START_MESSAGE = 0x0B; // first character of a new message
    private char END_OF_RECORD = 0x0D; // character sent between messages
    private char END_OF_SEGMENT = 0x0D; // character sent between hl7 segments

    public static void main(String args[]) throws Exception {

        if (args.length < 1) {
            System.out.println("Correct usage:  java SimpleReceiver <port> [time in seconds]");
            return;
        }
        SimpleReceiver simpleReceiver = new SimpleReceiver();
        int time = 0;
        if (args.length > 1)
            time = Integer.parseInt(args[1]);
        simpleReceiver.receive(Integer.parseInt(args[0]), time);
    }

    public void receive(int port, final int time) throws Exception {
        startTime = System.currentTimeMillis();
        lastTime = startTime;
        ServerSocket ssock = new ServerSocket(port);
        System.out.println("Server listening on port " + port);

        // display stats periodically
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(10000);

                        long now = System.currentTimeMillis();

                        int diffCount = count - lastCount;
                        long diffTime = now - lastTime;
                        double msgsPerSec = 1000.0 * diffCount / diffTime;
                        long allDiffTime = now - startTime;
                        double allMsgsPerSec = 1000.0 * count / allDiffTime;
                        System.out.println("==== " + count + " messages received, avg last 10 sec: " + Math.round(msgsPerSec * 100.0) / 100.0 + " msgs/sec, avg overall: " + Math.round(allMsgsPerSec * 100.0) / 100.0 + " msgs/sec (" + Thread.activeCount() + " threads).");

                        lastCount = count;
                        lastTime = now;
                    }
                } catch (InterruptedException e) {
                    // should happen when closed.
                }
            }
        }.start();

        new Thread() {
            public void run() {
                while (true) {
                    if (time != 0 && (System.currentTimeMillis() - startTime) >= (time * 1000)) {
                        double messagesPerSec = (count * 1000) / (System.currentTimeMillis() - startTime);
                        String output = "Messages received: " + count + "  -  Messages/Second: " + Math.round(messagesPerSec * 100.0) / 100.0;
                        System.out.println(output);
                        File outputFile = new File("output.txt");

                        try {
                            FileWriter writer = new FileWriter(outputFile, true);
                            writer.write(output + "\r\n");
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        System.exit(0);
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }.start();

        while (true) {
            final Socket sock = ssock.accept();
            new Thread() {
                public void run() {
                    try {
                        serve(sock.getInputStream(), new PrintStream(sock.getOutputStream()));
                        sock.close();
                    } catch (Exception e) {
                        System.out.println("Exception: " + e);
                    }
                }
            }.start();
        }
    }

    public void serve(InputStream in, PrintStream out) {
        out.println(START_MESSAGE + "MSH|^~\\&|HL7LISTENER|AMRS|FORMENTRY|AMRS|20050217153200||ORU^R01^ACK|?|P|2.5" + END_OF_SEGMENT + "MSA|AA|AMRS20050217152845|" + END_OF_SEGMENT + END_MESSAGE + END_OF_RECORD);
        incrementCount();
    }

    public synchronized void incrementCount() {
        count++;
    }
}
