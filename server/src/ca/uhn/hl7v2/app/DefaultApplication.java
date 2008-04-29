/**
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. 

The Original Code is "DefaultApplication.java".  Description: 
"An Application that does nothing with the message and returns an Application 
 Reject message in response." 

The Initial Developer of the Original Code is University Health Network. Copyright (C) 
2002.  All Rights Reserved. 

Contributor(s): ______________________________________. 

*/

package ca.uhn.hl7v2.app;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.primitive.CommonTS;
import ca.uhn.hl7v2.sourcegen.SourceGenerator;
import ca.uhn.hl7v2.util.MessageIDGenerator;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.log.HapiLog;
import ca.uhn.log.HapiLogFactory;

/**
 * An Application that does nothing with the message and returns an Application 
 * Reject message in response.  To be used when there are no other Applications 
 * that can process a given message.  
 * @author  Bryan Tripp
 */
public class DefaultApplication implements Application {

    private static final HapiLog log = HapiLogFactory.getHapiLog(DefaultApplication.class);

    /** Creates a new instance of DefaultApplication */
    public DefaultApplication() {
    }

    /**
     * Returns true.  
     */
    public boolean canProcess(Message in) {
        return true;
    }

    /**
     * Creates and returns an acknowledgement -- the details are determined by fillDetails().
     */
    public Message processMessage(Message in) throws ApplicationException {
        Message out = null;
        try {
            //get default ACK
            out = makeACK((Segment) in.get("MSH"), "CA", "");
            fillDetails(out);
        } catch (Exception e) {
            throw new ApplicationException("Couldn't create response message: " + e.getMessage());
        }        
        return out;
    } 
    
    /**
     * Fills in the details of an Application Reject message, including response and 
     * error codes, and a text error message.  This is the method to override if you want
     * to respond differently.  
     */
    public void fillDetails(Message ack) throws ApplicationException {
        try {
            //populate MSA and ERR with generic error ... 
            Segment msa = (Segment) ack.get("MSA");
            Terser.set(msa, 1, 0, 1, 1, "AR");
            Terser.set(
                msa,
                3,
                0,
                1,
                1,
                "No appropriate destination could be found to which this message could be routed.");
            //this is max length

            //populate ERR segment if it exists (may not depending on version)
            Structure s = ack.get("ERR");
            if (s != null) {
                Segment err = (Segment) s;
                Terser.set(err, 1, 0, 4, 1, "207");
                Terser.set(err, 1, 0, 4, 2, "Application Internal Error");
                Terser.set(err, 1, 0, 4, 3, "HL70357");
            }

        }
        catch (Exception e) {
            throw new ApplicationException("Error trying to create Application Reject message: " + e.getMessage());
        }
    }

    /**
     * Creates an ACK message with the minimum required information from an inbound message.  
     * Optional fields can be filled in afterwards, before the message is returned.  Pleaase   
     * note that MSH-10, the outbound message control ID, is also set using the class 
     * <code>ca.uhn.hl7v2.util.MessageIDGenerator</code>.  Also note that the ACK messages returned
     * is the same version as the version stated in the inbound MSH if there is a generic ACK for that
     * version, otherwise a version 2.4 ACK is returned. MSA-1 is set to AA by default.  
     *
     * @param inboundHeader the MSH segment if the inbound message
     * @throws IOException if there is a problem reading or writing the message ID file
     * @throws DataTypeException if there is a problem setting ACK values
     */
    public static Message makeACK(Segment inboundHeader, String statusCode, String textMessage ) throws HL7Exception, IOException {
        if (!inboundHeader.getName().equals("MSH"))
            throw new HL7Exception(
                "Need an MSH segment to create a response ACK (got " + inboundHeader.getName() + ")");

        //make ACK of correct version
        String version = null;
        try {
            version = Terser.get(inboundHeader, 12, 0, 1, 1);
        }
        catch (HL7Exception e) { /* proceed with null */
        }
        if (version == null) version = "2.4";

        String ackClassName = SourceGenerator.getVersionPackageName(version) + "message.ACK";

        Message out = null;
        try {
            Class ackClass = Class.forName(ackClassName);
            out = (Message) ackClass.newInstance();
        }
        catch (Exception e) {
            throw new HL7Exception("Can't instantiate ACK of class " + ackClassName + ": " + e.getClass().getName());
        }
        Terser terser = new Terser(out);

        //populate outbound MSH using data from inbound message ...             
        Segment outHeader = (Segment) out.get("MSH");
        fillResponseHeader(inboundHeader, outHeader);

        terser.set("/MSH-9", "ACK");
        terser.set("/MSH-12", version);
        terser.set("/MSA-1", statusCode);
        terser.set("/MSA-2", terser.get(inboundHeader, 10, 0, 1, 1));
        terser.set("/MSA-3", textMessage);
        
        terser.set("/MSH-3", terser.get(inboundHeader, 5, 0, 1, 1));
        terser.set("/MSH-4", terser.get(inboundHeader, 6, 0, 1, 1));
        terser.set("/MSH-5", terser.get(inboundHeader, 3, 0, 1, 1));
        terser.set("/MSH-6", terser.get(inboundHeader, 4, 0, 1, 1));
        terser.set("/MSH-10", terser.get(inboundHeader, 10, 0, 1, 1));

        return out;
    }

    /** 
     * Populates certain required fields in a response message header, using 
     * information from the corresponding inbound message.  The current time is 
     * used for the message time field, and <code>MessageIDGenerator</code> is 
     * used to create a unique message ID.  Version and message type fields are 
     * not populated.  
     */
    public static void fillResponseHeader(Segment inbound, Segment outbound) throws HL7Exception, IOException {
        if (!inbound.getName().equals("MSH") || !outbound.getName().equals("MSH"))
            throw new HL7Exception("Need MSH segments.  Got " + inbound.getName() + " and " + outbound.getName());

        //get MSH data from incoming message ...        
        String encChars = Terser.get(inbound, 2, 0, 1, 1);
        String fieldSep = Terser.get(inbound, 1, 0, 1, 1);
        String procID = Terser.get(inbound, 11, 0, 1, 1);

        //populate outbound MSH using data from inbound message ...                     
        Terser.set(outbound, 2, 0, 1, 1, encChars);
        Terser.set(outbound, 1, 0, 1, 1, fieldSep);
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(new Date());
        Terser.set(outbound, 7, 0, 1, 1, CommonTS.toHl7TSFormat(now));
        Terser.set(outbound, 10, 0, 1, 1, MessageIDGenerator.getInstance().getNewID());
        Terser.set(outbound, 11, 0, 1, 1, procID);
    }

}
