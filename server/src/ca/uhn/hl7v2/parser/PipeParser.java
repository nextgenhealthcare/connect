/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "PipeParser.java".  Description:
 * "An implementation of Parser that supports traditionally encoded (i.e"
 *
 * The Initial Developer of the Original Code is University Health Network. Copyright (C)
 * 2001.  All Rights Reserved.
 *
 * Contributor(s): Kenneth Beaton.
 *
 *
 */

package ca.uhn.hl7v2.parser;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.util.FilterIterator;
import ca.uhn.hl7v2.util.MessageIterator;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.log.HapiLog;
import ca.uhn.log.HapiLogFactory;

/**
 * An implementation of Parser that supports traditionally encoded (ie delimited with characters
 * like |, ^, and ~) HL7 messages.  Unexpected segments and fields are parsed into generic elements
 * that are added to the message.  
 * @author Bryan Tripp (bryan_tripp@sourceforge.net)
 */
public class PipeParser extends Parser {
    
    private static final HapiLog log = HapiLogFactory.getHapiLog(PipeParser.class);
    
    private final static String segDelim = "\r"; //see section 2.8 of spec
    
    /** Creates a new PipeParser */
    public PipeParser() {
    }
    
    /**
     * Returns a String representing the encoding of the given message, if
     * the encoding is recognized.  For example if the given message appears
     * to be encoded using HL7 2.x XML rules then "XML" would be returned.
     * If the encoding is not recognized then null is returned.  That this
     * method returns a specific encoding does not guarantee that the
     * message is correctly encoded (e.g. well formed XML) - just that
     * it is not encoded using any other encoding than the one returned.
     */
    public String getEncoding(String message) {
        String encoding = null;
        
        //quit if the string is too short
        if (message.length() < 4)
            return null;
        
        //see if it looks like this message is | encoded ...
        boolean ok = true;
        
        //string should start with "MSH"
        if (!message.startsWith("MSH"))
            return null;
        
        //4th character of each segment should be field delimiter
        char fourthChar = message.charAt(3);
        StringTokenizer st = new StringTokenizer(message, String.valueOf(segDelim), false);
        while (st.hasMoreTokens()) {
            String x = st.nextToken();
            if (x.length() > 0) {
                if (Character.isWhitespace(x.charAt(0)))
                    x = stripLeadingWhitespace(x);
                if (x.length() >= 4 && x.charAt(3) != fourthChar)
                    return null;
            }
        }
        
        //should be at least 11 field delimiters (because MSH-12 is required)
        int nextFieldDelimLoc = 0;
        for (int i = 0; i < 11; i++) {
            nextFieldDelimLoc = message.indexOf(fourthChar, nextFieldDelimLoc + 1);
            if (nextFieldDelimLoc < 0)
                return null;
        }
        
        if (ok)
            encoding = "VB";
        
        return encoding;
    }
    
    /**
     * @return the preferred encoding of this Parser
     */
    public String getDefaultEncoding() {
        return "VB";
    }
    
    /**
     * Returns true if and only if the given encoding is supported
     * by this Parser.
     */
    public boolean supportsEncoding(String encoding) {
        boolean supports = false;
        if (encoding != null && encoding.equals("VB"))
            supports = true;
        return supports;
    }
    
    /**
     * @deprecated this method should not be public 
     * @param message
     * @return
     * @throws HL7Exception
     * @throws EncodingNotSupportedException
     */
    public String getMessageStructure(String message) throws HL7Exception, EncodingNotSupportedException {
        return getStructure(message).messageStructure;
    }
    
    /**
     * @returns the message structure from MSH-9-3
     */
    private MessageStructure getStructure(String message) throws HL7Exception, EncodingNotSupportedException {
        EncodingCharacters ec = getEncodingChars(message);
        String messageStructure = null;
        boolean explicityDefined = true;
        String wholeFieldNine;
        try {
            String[] fields = split(message.substring(0, Math.max(message.indexOf(segDelim), message.length())),
                String.valueOf(ec.getFieldSeparator()));
            wholeFieldNine = fields[8];
            
            //message structure is component 3 but we'll accept a composite of 1 and 2 if there is no component 3 ...
            //      if component 1 is ACK, then the structure is ACK regardless of component 2
            String[] comps = split(wholeFieldNine, String.valueOf(ec.getComponentSeparator()));
            if (comps.length >= 3) {
                messageStructure = comps[2];
            } else if (comps.length > 0 && comps[0] != null && comps[0].equals("ACK")) {
                messageStructure = "ACK";
            } else if (comps.length == 2) {
                explicityDefined = false;
                messageStructure = comps[0] + "_" + comps[1];
            }
            /*else if (comps.length == 1 && comps[0] != null && comps[0].equals("ACK")) {
                messageStructure = "ACK"; //it's common for people to only populate component 1 in an ACK msg
            }*/
            else {
                StringBuffer buf = new StringBuffer("Can't determine message structure from MSH-9: ");
                buf.append(wholeFieldNine);
                if (comps.length < 3) {
                    buf.append(" HINT: there are only ");
                    buf.append(comps.length);
                    buf.append(" of 3 components present");
                }
                throw new HL7Exception(buf.toString(), HL7Exception.UNSUPPORTED_MESSAGE_TYPE);
            }            
        }
        catch (IndexOutOfBoundsException e) {
            throw new HL7Exception(
            "Can't find message structure (MSH-9-3): " + e.getMessage(),
            HL7Exception.UNSUPPORTED_MESSAGE_TYPE);
        }
        
        return new MessageStructure(messageStructure, explicityDefined);
    }
    
    /**
     * Returns object that contains the field separator and encoding characters
     * for this message.
     */
    private static EncodingCharacters getEncodingChars(String message) {
        return new EncodingCharacters(message.charAt(3), message.substring(4, 8));
    }
    
    /**
     * Parses a message string and returns the corresponding Message
     * object.  Unexpected segments added at the end of their group.  
     *
     * @throws HL7Exception if the message is not correctly formatted.
     * @throws EncodingNotSupportedException if the message encoded
     *      is not supported by this parser.
     */
    protected Message doParse(String message, String version) throws HL7Exception, EncodingNotSupportedException {
        
        //try to instantiate a message object of the right class
        MessageStructure structure = getStructure(message);
        Message m = instantiateMessage(structure.messageStructure, version, structure.explicitlyDefined);
        
        //MessagePointer ptr = new MessagePointer(this, m, getEncodingChars(message));
        MessageIterator messageIter = new MessageIterator(m, "MSH", true);
        FilterIterator.Predicate segmentsOnly = new FilterIterator.Predicate() {
            public boolean evaluate(Object obj) {
                if (Segment.class.isAssignableFrom(obj.getClass())) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        FilterIterator segmentIter = new FilterIterator(messageIter, segmentsOnly);
        
        String[] segments = split(message, segDelim);
        for (int i = 0; i < segments.length; i++) {
            
            //get rid of any leading whitespace characters ...
            if (segments[i] != null && segments[i].length() > 0 && Character.isWhitespace(segments[i].charAt(0)))
                segments[i] = stripLeadingWhitespace(segments[i]);
            
            //sometimes people put extra segment delimiters at end of msg ...
            char delim = '|';
            if (segments[i] != null && segments[i].length() >= 3) {
                final String name;
                if (i == 0) {
                    name = segments[i].substring(0, 3);
                    delim = segments[i].charAt(3);
                } else if (segments[i].length() > 3) {
                    name = segments[i].substring(0, segments[i].indexOf(delim));
                } else{
                	//if the segment is only 3 chars, don't crash
                	//MIRTH-235
                	name = segments[i];
                }
                
                log.debug("Parsing segment " + name);
                
                messageIter.setDirection(name);
                FilterIterator.Predicate byDirection = new FilterIterator.Predicate() {
                    public boolean evaluate(Object obj) {
                        Structure s = (Structure) obj;
                        log.debug("PipeParser iterating message in direction " + name + " at " + s.getName());
                        if (s.getName().matches(name + "\\d*")) {
                            return true;
                        } else {
                            return false;
                        }
                    }                    
                };
                FilterIterator dirIter = new FilterIterator(segmentIter, byDirection);
                if (dirIter.hasNext()) {
                    parse((Segment) dirIter.next(), segments[i], getEncodingChars(message));
                }
            }
        }
        return m;
    }
    
    /**
     * Parses a segment string and populates the given Segment object.  Unexpected fields are
     * added as Varies' at the end of the segment.  
     *
     * @throws HL7Exception if the given string does not contain the
     *      given segment or if the string is not encoded properly
     */
    public void parse(Segment destination, String segment, EncodingCharacters encodingChars) throws HL7Exception {
        int fieldOffset = 0;
        if (isDelimDefSegment(destination.getName())) {
            fieldOffset = 1;
            //set field 1 to fourth character of string
            Terser.set(destination, 1, 0, 1, 1, String.valueOf(encodingChars.getFieldSeparator()));
        }
        
        String[] fields = split(segment, String.valueOf(encodingChars.getFieldSeparator()));
        //destination.setName(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            String[] reps = split(fields[i], String.valueOf(encodingChars.getRepetitionSeparator()));
            if (log.isDebugEnabled()) {
                log.debug(reps.length + "reps delimited by: " + encodingChars.getRepetitionSeparator());                
            }
            
            //MSH-2 will get split incorrectly so we have to fudge it ...
            boolean isMSH2 = isDelimDefSegment(destination.getName()) && i+fieldOffset == 2;
            if (isMSH2) {  
                reps = new String[1];
                reps[0] = fields[i];
            }
            
            for (int j = 0; j < reps.length; j++) {
                try {
                    StringBuffer statusMessage = new StringBuffer("Parsing field ");
                    statusMessage.append(i+fieldOffset);
                    statusMessage.append(" repetition ");
                    statusMessage.append(j);
                    log.debug(statusMessage.toString());
                    //parse(destination.getField(i + fieldOffset, j), reps[j], encodingChars, false);

                    Type field = destination.getField(i + fieldOffset, j);
                    if (isMSH2) {
                        Terser.getPrimitive(field, 1, 1).setValue(reps[j]);
                    } else {
                        parse(field, reps[j], encodingChars);
                    }
                }
                catch (HL7Exception e) {
                    //set the field location and throw again ...
                    e.setFieldPosition(i);
                    e.setSegmentRepetition(MessageIterator.getIndex(destination.getParent(), destination).rep);
                    e.setSegmentName(destination.getName());
                    throw e;
                }
            }
        }
        
        //set data type of OBX-5
        if (destination.getClass().getName().indexOf("OBX") >= 0) {
            Varies.fixOBX5(destination, getFactory());
        }
        
    }
    
    /** 
     * @return true if the segment is MSH, FHS, or BHS.  These need special treatment 
     *  because they define delimiters.
     * @param theSegmentName
     */
    private static boolean isDelimDefSegment(String theSegmentName) {
        boolean is = false;
        if (theSegmentName.equals("MSH") 
            || theSegmentName.equals("FHS") 
            || theSegmentName.equals("BHS")) 
        {
            is = true;
        }
        return is;
    }
    
    /**
     * Fills a field with values from an unparsed string representing the field.  
     * @param destinationField the field Type
     * @param data the field string (including all components and subcomponents; not including field delimiters)
     * @param encodingCharacters the encoding characters used in the message
     */
    private static void parse(Type destinationField, String data, EncodingCharacters encodingCharacters) throws HL7Exception {
        String[] components = split(data, String.valueOf(encodingCharacters.getComponentSeparator()));
        for (int i = 0; i < components.length; i++) {
            String[] subcomponents = split(components[i], String.valueOf(encodingCharacters.getSubcomponentSeparator()));
            for (int j = 0; j < subcomponents.length; j++) {
                String val = subcomponents[j];
                if (val != null) {
                    val = Escape.unescape(val, encodingCharacters);
                }
                Terser.getPrimitive(destinationField, i+1, j+1).setValue(val);                
            }
        }
    }
        
    /** Returns the component or subcomponent separator from the given encoding characters. */
    private static char getSeparator(boolean subComponents, EncodingCharacters encodingChars) {
        char separator;
        if (subComponents) {
            separator = encodingChars.getSubcomponentSeparator();
        }
        else {
            separator = encodingChars.getComponentSeparator();
        }
        return separator;
    }
    
    /**
     * Splits the given composite string into an array of components using the given
     * delimiter.
     */
    public static String[] split(String composite, String delim) {
        ArrayList components = new ArrayList();
        
        //defend against evil nulls
        if (composite == null)
            composite = "";
        if (delim == null)
            delim = "";
        
        StringTokenizer tok = new StringTokenizer(composite, delim, true);
        boolean previousTokenWasDelim = true;
        while (tok.hasMoreTokens()) {
            String thisTok = tok.nextToken();
            if (thisTok.equals(delim)) {
                if (previousTokenWasDelim)
                    components.add(null);
                previousTokenWasDelim = true;
            }
            else {
                components.add(thisTok);
                previousTokenWasDelim = false;
            }
        }
        
        String[] ret = new String[components.size()];
        for (int i = 0; i < components.size(); i++) {
            ret[i] = (String) components.get(i);
        }
        
        return ret;
    }
    
    /**
     * Encodes the given Type, using the given encoding characters. 
     * It is assumed that the Type represents a complete field rather than a component.
     */
    public static String encode(Type source, EncodingCharacters encodingChars) {
        StringBuffer field = new StringBuffer();
        for (int i = 1; i <= Terser.numComponents(source); i++) {
            StringBuffer comp = new StringBuffer();
            for (int j = 1; j <= Terser.numSubComponents(source, i); j++) {
                Primitive p = Terser.getPrimitive(source, i, j);
                comp.append(encodePrimitive(p, encodingChars));
                comp.append(encodingChars.getSubcomponentSeparator());
            }
            field.append(stripExtraDelimiters(comp.toString(), encodingChars.getSubcomponentSeparator()));
            field.append(encodingChars.getComponentSeparator());
        }
        return stripExtraDelimiters(field.toString(), encodingChars.getComponentSeparator());
        //return encode(source, encodingChars, false);
    }
    
    private static String encodePrimitive(Primitive p, EncodingCharacters encodingChars) {
        String val = ((Primitive) p).getValue();
        if (val == null) {
            val = "";
        } else {
            val = Escape.escape(val, encodingChars);
        }
        return val;
    }
    
    /**
     * Removes unecessary delimiters from the end of a field or segment.
     * This seems to be more convenient than checking to see if they are needed
     * while we are building the encoded string.
     */
    private static String stripExtraDelimiters(String in, char delim) {
        char[] chars = in.toCharArray();
        
        //search from back end for first occurance of non-delimiter ...
        int c = chars.length - 1;
        boolean found = false;
        while (c >= 0 && !found) {
            if (chars[c--] != delim)
                found = true;
        }
        
        String ret = "";
        if (found)
            ret = String.valueOf(chars, 0, c + 2);
        return ret;
    }
    
    /**
     * Formats a Message object into an HL7 message string using the given
     * encoding.
     * @throws HL7Exception if the data fields in the message do not permit encoding
     *      (e.g. required fields are null)
     * @throws EncodingNotSupportedException if the requested encoding is not
     *      supported by this parser.
     */
    protected String doEncode(Message source, String encoding) throws HL7Exception, EncodingNotSupportedException {
        if (!this.supportsEncoding(encoding))
            throw new EncodingNotSupportedException("This parser does not support the " + encoding + " encoding");
        
        return encode(source);
    }
    
    /**
     * Formats a Message object into an HL7 message string using this parser's
     * default encoding ("VB").
     * @throws HL7Exception if the data fields in the message do not permit encoding
     *      (e.g. required fields are null)
     */
    protected String doEncode(Message source) throws HL7Exception {
        //get encoding characters ...
        Segment msh = (Segment) source.get("MSH");
        String fieldSepString = Terser.get(msh, 1, 0, 1, 1);
        
        if (fieldSepString == null) 
            throw new HL7Exception("Can't encode message: MSH-1 (field separator) is missing");
        
        char fieldSep = '|';
        if (fieldSepString != null && fieldSepString.length() > 0)
            fieldSep = fieldSepString.charAt(0);
        
        String encCharString = Terser.get(msh, 2, 0, 1, 1);
        
        if (encCharString == null) 
            throw new HL7Exception("Can't encode message: MSH-2 (encoding characters) is missing");
                
        if (encCharString.length() != 4)
            throw new HL7Exception(
            "Encoding characters '" + encCharString + "' invalid -- must be 4 characters",
            HL7Exception.DATA_TYPE_ERROR);
        EncodingCharacters en = new EncodingCharacters(fieldSep, encCharString);
        
        //pass down to group encoding method which will operate recursively on children ...
        return encode((Group) source, en);
    }
    
    /**
     * Returns given group serialized as a pipe-encoded string - this method is called
     * by encode(Message source, String encoding).
     */
    public static String encode(Group source, EncodingCharacters encodingChars) throws HL7Exception {
        StringBuffer result = new StringBuffer();
        
        String[] names = source.getNames();
        for (int i = 0; i < names.length; i++) {
            Structure[] reps = source.getAll(names[i]);
            for (int rep = 0; rep < reps.length; rep++) {
                if (reps[rep] instanceof Group) {
                    result.append(encode((Group) reps[rep], encodingChars));
                }
                else {
                    String segString = encode((Segment) reps[rep], encodingChars);
                    if (segString.length() >= 4) {
                        result.append(segString);
                        result.append('\r');
                    }
                }
            }
        }
        return result.toString();
    }
    
    public static String encode(Segment source, EncodingCharacters encodingChars) {
        StringBuffer result = new StringBuffer();
        result.append(source.getName());
        result.append(encodingChars.getFieldSeparator());
        
        //start at field 2 for MSH segment because field 1 is the field delimiter
        int startAt = 1;
        if (isDelimDefSegment(source.getName()))
            startAt = 2;
        
        //loop through fields; for every field delimit any repetitions and add field delimiter after ...
        int numFields = source.numFields();
        for (int i = startAt; i <= numFields; i++) {
            try {
                Type[] reps = source.getField(i);
                for (int j = 0; j < reps.length; j++) {
                    String fieldText = encode(reps[j], encodingChars);
                    //if this is MSH-2, then it shouldn't be escaped, so unescape it again
                    if (isDelimDefSegment(source.getName()) && i == 2)
                        fieldText = Escape.unescape(fieldText, encodingChars);
                    result.append(fieldText);
                    if (j < reps.length - 1)
                        result.append(encodingChars.getRepetitionSeparator());
                }
            }
            catch (HL7Exception e) {
                log.error("Error while encoding segment: ", e);
            }
            result.append(encodingChars.getFieldSeparator());
        }
        
        //strip trailing delimiters ...
        return stripExtraDelimiters(result.toString(), encodingChars.getFieldSeparator());
    }
    
    /**
     * Removes leading whitespace from the given string.  This method was created to deal with frequent
     * problems parsing messages that have been hand-written in windows.  The intuitive way to delimit
     * segments is to hit <ENTER> at the end of each segment, but this creates both a carriage return
     * and a line feed, so to the parser, the first character of the next segment is the line feed.
     */
    public static String stripLeadingWhitespace(String in) {
        StringBuffer out = new StringBuffer();
        char[] chars = in.toCharArray();
        int c = 0;
        while (c < chars.length) {
            if (!Character.isWhitespace(chars[c]))
                break;
            c++;
        }
        for (int i = c; i < chars.length; i++) {
            out.append(chars[i]);
        }
        return out.toString();
    }
    
    /**
     * <p>Returns a minimal amount of data from a message string, including only the
     * data needed to send a response to the remote system.  This includes the
     * following fields:
     * <ul><li>field separator</li>
     * <li>encoding characters</li>
     * <li>processing ID</li>
     * <li>message control ID</li></ul>
     * This method is intended for use when there is an error parsing a message,
     * (so the Message object is unavailable) but an error message must be sent
     * back to the remote system including some of the information in the inbound
     * message.  This method parses only that required information, hopefully
     * avoiding the condition that caused the original error.  The other
     * fields in the returned MSH segment are empty.</p>
     */
    public Segment getCriticalResponseData(String message) throws HL7Exception {
        //try to get MSH segment
        int locStartMSH = message.indexOf("MSH");
        if (locStartMSH < 0)
            throw new HL7Exception(
            "Couldn't find MSH segment in message: " + message,
            HL7Exception.SEGMENT_SEQUENCE_ERROR);
        int locEndMSH = message.indexOf('\r', locStartMSH + 1);
        if (locEndMSH < 0)
            locEndMSH = message.length();
        String mshString = message.substring(locStartMSH, locEndMSH);
        
        //find out what the field separator is
        char fieldSep = mshString.charAt(3);
        
        //get field array
        String[] fields = split(mshString, String.valueOf(fieldSep));
        
        Segment msh = null;
        try {
            //parse required fields
            String encChars = fields[1];
            char compSep = encChars.charAt(0);
            String messControlID = fields[9];
            String[] procIDComps = {""};
            if (fields[10] != null)
            	procIDComps = split(fields[10], String.valueOf(compSep));
            
            String sendingApp = (fields[2] == null) ? "" : fields[2];
            String sendingFacility = (fields[3] == null) ? "" : fields[3];
            String receivingApp = (fields[4] == null) ? "" : fields[4];
            String receivingFacility = (fields[5] == null) ? "" : fields[5];
            
            //fill MSH segment
            String version = "2.4"; //default
            try {
                version = this.getVersion(message);
            }
            catch (Exception e) { /* use the default */
            }
            
            msh = Parser.makeControlMSH(version, getFactory());
            
            Terser.set(msh, 1, 0, 1, 1, String.valueOf(fieldSep));
            Terser.set(msh, 2, 0, 1, 1, encChars);
            Terser.set(msh, 3, 0, 1, 1, sendingApp);
            Terser.set(msh, 4, 0, 1, 1, sendingFacility);
            Terser.set(msh, 5, 0, 1, 1, receivingApp);
            Terser.set(msh, 6, 0, 1, 1, receivingFacility);
            Terser.set(msh, 10, 0, 1, 1, messControlID);
            Terser.set(msh, 11, 0, 1, 1, procIDComps[0]);
            Terser.set(msh, 12, 0, 1, 1, version);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new HL7Exception(
            "Can't parse critical fields from MSH segment ("
            + e.getClass().getName()
            + ": "
            + e.getMessage()
            + "): "
            + mshString,
            HL7Exception.REQUIRED_FIELD_MISSING, e);
        }
        
        return msh;
    }
    
    /**
     * For response messages, returns the value of MSA-2 (the message ID of the message
     * sent by the sending system).  This value may be needed prior to main message parsing,
     * so that (particularly in a multi-threaded scenario) the message can be routed to
     * the thread that sent the request.  We need this information first so that any
     * parse exceptions are thrown to the correct thread.
     * Returns null if MSA-2 can not be found (e.g. if the message is not a
     * response message).
     */
    public String getAckID(String message) {
        String ackID = null;
        int startMSA = message.indexOf("\rMSA");
        if (startMSA >= 0) {
            int startFieldOne = startMSA + 5;
            char fieldDelim = message.charAt(startFieldOne - 1);
            int start = message.indexOf(fieldDelim, startFieldOne) + 1;
            int end = message.indexOf(fieldDelim, start);
            int segEnd = message.indexOf(String.valueOf(segDelim), start);
            if (segEnd > start && segEnd < end)
                end = segEnd;
            
            //if there is no field delim after MSH-2, need to go to end of message, but not including end seg delim if it exists
            if (end < 0) {
                if (message.charAt(message.length() - 1) == '\r') {
                    end = message.length() - 1;
                }
                else {
                    end = message.length();
                }
            }
            if (start > 0 && end > start) {
                ackID = message.substring(start, end);
            }
        }
        log.debug("ACK ID: " + ackID);
        return ackID;
    }
    
    /**
     * Returns the version ID (MSH-12) from the given message, without fully parsing the message.
     * The version is needed prior to parsing in order to determine the message class
     * into which the text of the message should be parsed.
     * @throws HL7Exception if the version field can not be found.
     */
    /**
     * Returns the version ID (MSH-12) from the given message, without fully parsing the message.
     * The version is needed prior to parsing in order to determine the message class
     * into which the text of the message should be parsed.
     * @throws HL7Exception if the version field can not be found.
     */
    public String getVersion(String message) throws HL7Exception {
        int startMSH = message.indexOf("MSH");
        int endMSH = message.indexOf(PipeParser.segDelim, startMSH);
        if (endMSH < 0)
            endMSH = message.length();
        String msh = message.substring(startMSH, endMSH);
        String fieldSep = null;
        if (msh.length() > 3) {
            fieldSep = String.valueOf(msh.charAt(3));
        }
        else {
            throw new HL7Exception("Can't find field separator in MSH: " + msh, HL7Exception.UNSUPPORTED_VERSION_ID);
        }
        
        String[] fields = split(msh, fieldSep);
        
        String compSep = null;
        if (fields.length >= 2 && fields[1] != null && fields[1].length() == 4) {
            compSep = String.valueOf(fields[1].charAt(0)); //get component separator as 1st encoding char
        } 
        else {
            throw new HL7Exception("Invalid or incomplete encoding characters - MSH-2 is " + fields[1],  
                    HL7Exception.REQUIRED_FIELD_MISSING);
        }
        
        String version = null;
        if (fields.length >= 12) {
        	String[] comp = split(fields[11], compSep);
        	if (comp.length >= 1) {
        		version = comp[0];
        	} else {
        		throw new HL7Exception("Can't find version ID - MSH.12 is " + fields[11],
        				HL7Exception.REQUIRED_FIELD_MISSING);
        	}
        }
        else {
            throw new HL7Exception(
            "Can't find version ID - MSH has only " + fields.length + " fields.",
            HL7Exception.REQUIRED_FIELD_MISSING);
        }
        return version;
    }

    
    /**
     * A struct for holding a message class string and a boolean indicating whether it 
     * was defined explicitly.  
     */
    private static class MessageStructure {
        public String messageStructure;
        public boolean explicitlyDefined;
        
        public MessageStructure(String theMessageStructure, boolean isExplicitlyDefined) {
            messageStructure = theMessageStructure;
            explicitlyDefined = isExplicitlyDefined;
        }
    }
    
}
