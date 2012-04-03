package com.mirth.connect.simplesender;

import java.io.DataOutputStream;
import java.net.Socket;

public class TCPSender {
    public boolean send(String hl7, String outputIP, String outputPort) throws Exception {
        Socket s = null;
        try {
            s = new Socket(outputIP, Integer.parseInt(outputPort));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.write(LLPUtil.HL7Encode(hl7).getBytes());
        } finally {
            if (s != null) {
                s.close();
            }
        }

        return true;
    }
}
