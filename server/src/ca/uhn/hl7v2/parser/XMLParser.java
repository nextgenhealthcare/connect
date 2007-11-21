/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "XMLParser.java".  Description:
 * "Parses and encodes HL7 messages in XML form, according to HL7's normative XML encoding
 * specification."
 *
 * The Initial Developer of the Original Code is University Health Network. Copyright (C)
 * 2002.  All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 *
 */

package ca.uhn.hl7v2.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.GenericComposite;
import ca.uhn.hl7v2.model.GenericPrimitive;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.log.HapiLog;
import ca.uhn.log.HapiLogFactory;

/**
 * Parses and encodes HL7 messages in XML form, according to HL7's normative XML encoding
 * specification.  This is an abstract class that handles datatype and segment parsing/encoding,
 * but not the parsing/encoding of entire messages.  To use the XML parser, you should create a
 * subclass for a certain message structure.  This subclass must be able to identify the Segment
 * objects that correspond to various Segment nodes in an XML document, and call the methods <code>
 * parse(Segment segment, ElementNode segmentNode)</code> and <code>encode(Segment segment, ElementNode segmentNode)
 * </code> as appropriate.  XMLParser uses the Xerces parser, which must be installed in your classpath.
 * @author Bryan Tripp, Shawn Bellina
 */
public abstract class XMLParser extends Parser {

    private static final HapiLog log = HapiLogFactory.getHapiLog(XMLParser.class);

    private DOMParser parser;

    /**
     * The nodes whose names match these strings will be kept as original, 
     * meaning that no white space treaming will occur on them
     */
    private String[] keepAsOriginalNodes;

    /**
     * All keepAsOriginalNodes names, concatenated by a pipe (|)
     */
    private String concatKeepAsOriginalNodes = "";

    public XMLParser() {
        parser = new DOMParser(new StandardParserConfiguration());
        try {
            parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        }
        catch (Exception e) {
            log.error("Can't exclude whitespace from XML DOM", e);
        }
    }

    /**
     * Returns a String representing the encoding of the given message, if
     * the encoding is recognized.  For example if the given message appears
     * to be encoded using HL7 2.x XML rules then "XML" would be returned.
     * If the encoding is not recognized then null is returned.  That this
     * method returns a specific encoding does not guarantee that the
     * message is correctly encoded (e.g. well formed XML) - just that
     * it is not encoded using any other encoding than the one returned.
     * Returns null if the encoding is not recognized.
     */
    public String getEncoding(String message) {
        String encoding = null;

        //check for a number of expected strings 
        String[] expected = { "(?s).*<([0-9a-zA-Z][-\\w]*:)?MSH\\.1.*", "(?s).*<([0-9a-zA-Z][-\\w]*:)?MSH\\.2.*", "(?s).*</([0-9a-zA-Z][-\\w]*:)?MSH>.*" };
        boolean isXML = true;
        for (int i = 0; i < expected.length; i++) {
            if (!message.matches(expected[i])) {
                isXML = false;
            }
        }
        if (isXML)
            encoding = "XML";

        return encoding;
    }

    /**
     * Returns true if and only if the given encoding is supported
     * by this Parser.
     */
    public boolean supportsEncoding(String encoding) {
        if (encoding != null && encoding.equals("XML")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return the preferred encoding of this Parser
     */
    public String getDefaultEncoding() {
        return "XML";
    }
    
    /**
     * Sets the <i>keepAsOriginalNodes<i>
     * 
     * The nodes whose names match the <i>keepAsOriginalNodes<i> will be kept as original, 
     * meaning that no white space treaming will occur on them
     */
    public void setKeepAsOriginalNodes(String[] keepAsOriginalNodes) {
        this.keepAsOriginalNodes = keepAsOriginalNodes;

        if (keepAsOriginalNodes.length != 0) {
            //initializes the         
            StringBuffer strBuf = new StringBuffer(keepAsOriginalNodes[0]);
            for (int i = 1; i < keepAsOriginalNodes.length; i++) {
                strBuf.append("|");
                strBuf.append(keepAsOriginalNodes[i]);
            }
            concatKeepAsOriginalNodes = strBuf.toString();
        }
        else {
            concatKeepAsOriginalNodes = "";
        }
    }

    /**
     * Sets the <i>keepAsOriginalNodes<i>
     */
    public String[] getKeepAsOriginalNodes() {
        return keepAsOriginalNodes;
    }

    /**
     * <p>Creates and populates a Message object from an XML Document that contains an XML-encoded HL7 message.</p>
     * <p>The easiest way to implement this method for a particular message structure is as follows:
     * <ol><li>Create an instance of the Message type you are going to handle with your subclass
     * of XMLParser</li>
     * <li>Go through the given Document and find the Elements that represent the top level of
     * each message segment. </li>
     * <li>For each of these segments, call <code>parse(Segment segmentObject, Element segmentElement)</code>,
     * providing the appropriate Segment from your Message object, and the corresponding Element.</li></ol>
     * At the end of this process, your Message object should be populated with data from the XML
     * Document.</p>
     * @throws HL7Exception if the message is not correctly formatted.
     * @throws EncodingNotSupportedException if the message encoded
     *      is not supported by this parser.
     */
    public abstract Message parseDocument(Document XMLMessage, String version) throws HL7Exception;

    /**
     * <p>Parses a message string and returns the corresponding Message
     * object.  This method checks that the given message string is XML encoded, creates an
     * XML Document object (using Xerces) from the given String, and calls the abstract
     * method <code>parse(Document XMLMessage)</code></p>
     */
    protected Message doParse(String message, String version) throws HL7Exception, EncodingNotSupportedException {
        Message m = null;

        //parse message string into a DOM document 
        try {
            Document doc = null;
            synchronized (this) {
                parser.parse(new InputSource(new StringReader(message)));
                doc = parser.getDocument();
            }
            m = parseDocument(doc, version);
        }
        catch (SAXException e) {
            throw new HL7Exception("SAXException parsing XML", HL7Exception.APPLICATION_INTERNAL_ERROR, e);
        }
        catch (IOException e) {
            throw new HL7Exception("IOException parsing XML", HL7Exception.APPLICATION_INTERNAL_ERROR, e);
        }

        return m;
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
        if (!encoding.equals("XML"))
            throw new EncodingNotSupportedException("XMLParser supports only XML encoding");
        return encode(source);
    }

    /**
     * Formats a Message object into an HL7 message string using this parser's
     * default encoding (XML encoding). This method calls the abstract method
     * <code>encodeDocument(...)</code> in order to obtain XML Document object
     * representation of the Message, then serializes it to a String.
     * @throws HL7Exception if the data fields in the message do not permit encoding
     *      (e.g. required fields are null)
     */
    protected String doEncode(Message source) throws HL7Exception {
     //   if (source instanceof GenericMessage) {
    //        throw new HL7Exception("Can't XML-encode a GenericMessage.  Message must have a recognized structure.");
    //    }
        
        Document doc = encodeDocument(source);
        doc.getDocumentElement().setAttribute("xmlns", "urn:hl7-org:v2xml");
        
        StringWriter out = new StringWriter();

        OutputFormat outputFormat = new OutputFormat("", null, true);

        XMLSerializer ser = new XMLSerializer(out, outputFormat); //default output format
        try {
            ser.serialize(doc);
        }
        catch (IOException e) {
            throw new HL7Exception(
                "IOException serializing XML document to string",
                HL7Exception.APPLICATION_INTERNAL_ERROR,
                e);
        }
        return out.toString();
    }

    /**
     * <p>Creates an XML Document that corresponds to the given Message object. </p>
     * <p>If you are implementing this method, you should create an XML Document, and insert XML Elements
     * into it that correspond to the groups and segments that belong to the message type that your subclass
     * of XMLParser supports.  Then, for each segment in the message, call the method
     * <code>encode(Segment segmentObject, Element segmentElement)</code> using the Element for
     * that segment and the corresponding Segment object from the given Message.</p>
     */
    public abstract Document encodeDocument(Message source) throws HL7Exception;

    /** 
     * Populates the given Segment object with data from the given XML Element.
     * @throws HL7Exception if the XML Element does not have the correct name and structure
     *      for the given Segment, or if there is an error while setting individual field values.
     */
    public void parse(Segment segmentObject, Element segmentElement) throws HL7Exception {
        HashSet done = new HashSet();
        
//        for (int i = 1; i <= segmentObject.numFields(); i++) {
//            String elementName = makeElementName(segmentObject, i);
//            done.add(elementName);
//            parseReps(segmentObject, segmentElement, elementName, i);
//        }
        
        NodeList all = segmentElement.getChildNodes();
        for (int i = 0; i < all.getLength(); i++) {
            String elementName = all.item(i).getLocalName();
            if (all.item(i).getNodeType() == Node.ELEMENT_NODE && !done.contains(elementName)) {
                done.add(elementName);
                
                int index = elementName.indexOf('.');
                if (index >= 0 && elementName.length() > index) { //properly formatted element
                    String fieldNumString = elementName.substring(index + 1);
                    int fieldNum = Integer.parseInt(fieldNumString);
                    parseReps(segmentObject, segmentElement, elementName, fieldNum);                        
                } else {                        
                    log.debug("Child of segment " + segmentObject.getName() 
                            + " doesn't look like a field: " + elementName);
                }
            }
        }

        //set data type of OBX-5        
        if (segmentObject.getClass().getName().indexOf("OBX") >= 0) {
            Varies.fixOBX5(segmentObject, getFactory());
        }
    }
    
    private void parseReps(Segment segmentObject, Element segmentElement, String fieldName, int fieldNum) 
             throws DataTypeException, HL7Exception {
        
        NodeList reps = segmentElement.getElementsByTagNameNS( "*", fieldName);
        for (int i = 0; i < reps.getLength(); i++) {
            parse(segmentObject.getField(fieldNum, i), (Element) reps.item(i));
        }        
    }

    /**
     * Populates the given Element with data from the given Segment, by inserting
     * Elements corresponding to the Segment's fields, their components, etc.  Returns 
     * true if there is at least one data value in the segment.   
     */
    public boolean encode(Segment segmentObject, Element segmentElement) throws HL7Exception {
        boolean hasValue = false;
        int n = segmentObject.numFields();
        for (int i = 1; i <= n; i++) {
            String name = makeElementName(segmentObject, i);
            Type[] reps = segmentObject.getField(i);
            for (int j = 0; j < reps.length; j++) {
                Element newNode = segmentElement.getOwnerDocument().createElement(name);
                boolean componentHasValue = encode(reps[j], newNode);
                if (componentHasValue) {
                    try {
                        segmentElement.appendChild(newNode);
                    }
                    catch (DOMException e) {
                        throw new HL7Exception(
                            "DOMException encoding Segment: ",
                            HL7Exception.APPLICATION_INTERNAL_ERROR,
                            e);
                    }
                    hasValue = true;
                }
            }
        }
        return hasValue;
    }

    /**
     * Populates the given Type object with data from the given XML Element.
     */
    public void parse(Type datatypeObject, Element datatypeElement) throws DataTypeException {
        if (datatypeObject instanceof Varies) {
            parseVaries((Varies) datatypeObject, datatypeElement);
        }
        else if (datatypeObject instanceof Primitive) {
            parsePrimitive((Primitive) datatypeObject, datatypeElement);
        }
        else if (datatypeObject instanceof Composite) {
            parseComposite((Composite) datatypeObject, datatypeElement);
        }
    }

    /**
     * Parses an XML element into a Varies by determining whether the element is primitive or 
     * composite, calling setData() on the Varies with a new generic primitive or composite as appropriate, 
     * and then calling parse again with the new Type object.  
     */
    private void parseVaries(Varies datatypeObject, Element datatypeElement) throws DataTypeException {
        //figure out what data type it holds 
        //short nodeType = datatypeElement.getFirstChild().getNodeType();        
        if (!hasChildElement(datatypeElement)) {
            //it's a primitive 
            datatypeObject.setData(new GenericPrimitive(datatypeObject.getMessage()));
        }
        else {
            //it's a composite ... almost know what type, except that we don't have the version here 
            datatypeObject.setData(new GenericComposite(datatypeObject.getMessage()));
        }
        parse(datatypeObject.getData(), datatypeElement);
    }

    /** Returns true if any of the given element's children are elements */
    private boolean hasChildElement(Element e) {
        NodeList children = e.getChildNodes();
        boolean hasElement = false;
        int c = 0;
        while (c < children.getLength() && !hasElement) {
            if (children.item(c).getNodeType() == Node.ELEMENT_NODE) {
                hasElement = true;
            }
            c++;
        }
        return hasElement;
    }

    /** Parses a primitive type by filling it with text child, if any */
    private void parsePrimitive(Primitive datatypeObject, Element datatypeElement) throws DataTypeException {
        NodeList children = datatypeElement.getChildNodes();
        int c = 0;
        boolean full = false;
        while (c < children.getLength() && !full) {
            Node child = children.item(c++);
            if (child.getNodeType() == Node.TEXT_NODE) {
                try {
                    if (child.getNodeValue() != null && !child.getNodeValue().equals("")) {
                        if (keepAsOriginal(child.getParentNode())) {
                            datatypeObject.setValue(child.getNodeValue());
                        }
                        else {
                            datatypeObject.setValue(removeWhitespace(child.getNodeValue()));
                        }
                    }
                }
                catch (DOMException e) {
                    log.error("Error parsing primitive value from TEXT_NODE", e);
                }
                full = true;
            }
        }
    }

    /**
     * Checks if <code>Node</code> content should be kept as original (ie.: whitespaces won't be removed)
     * 
     * @param node The target <code>Node</code> 
     * @return boolean <code>true</code> if whitespaces should not be removed from node content, 
     *                 <code>false</code> otherwise
     */
    protected boolean keepAsOriginal(Node node) {
        if (node.getLocalName() == null)
            return false;
        return concatKeepAsOriginalNodes.indexOf(node.getLocalName()) != -1;
    }

    /** 
     * Removes all unecessary whitespace from the given String (intended to be used with Primitive values).  
     * This includes leading and trailing whitespace, and repeated space characters.  Carriage returns, 
     * line feeds, and tabs are replaced with spaces. 
     */
    protected String removeWhitespace(String s) {
        s = s.replace('\r', ' ');
        s = s.replace('\n', ' ');
        s = s.replace('\t', ' ');

        boolean repeatedSpacesExist = true;
        while (repeatedSpacesExist) {
            int loc = s.indexOf("  ");
            if (loc < 0) {
                repeatedSpacesExist = false;
            }
            else {
                StringBuffer buf = new StringBuffer();
                buf.append(s.substring(0, loc));
                buf.append(" ");
                buf.append(s.substring(loc + 2));
                s = buf.toString();
            }
        }
        return s.trim();
    }

    /**
     * Populates a Composite type by looping through it's children, finding corresponding 
     * Elements among the children of the given Element, and calling parse(Type, Element) for
     * each.
     */
    private void parseComposite(Composite datatypeObject, Element datatypeElement) throws DataTypeException {
        if (datatypeObject instanceof GenericComposite) { //elements won't be named GenericComposite.x
            NodeList children = datatypeElement.getChildNodes();
            int compNum = 0;
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    parse(datatypeObject.getComponent(compNum), (Element) children.item(i));
                    compNum++;
                }
            }
        }
        else {
            Type[] children = datatypeObject.getComponents();
            for (int i = 0; i < children.length; i++) {
                NodeList matchingElements =
                    datatypeElement.getElementsByTagNameNS( "*", makeElementName(datatypeObject, i + 1));
                if (matchingElements.getLength() > 0) {
                    parse(children[i], (Element) matchingElements.item(0)); //components don't repeat - use 1st
                }
            }
        }
    }

    /** 
     * Returns the expected XML element name for the given child of a message constituent 
     * of the given class (the class should be a Composite or Segment class). 
     */
    /*private String makeElementName(Class c, int child) {
        String longClassName = c.getName();
        String shortClassName = longClassName.substring(longClassName.lastIndexOf('.') + 1, longClassName.length());
        if (shortClassName.startsWith("Valid")) {
            shortClassName = shortClassName.substring(5, shortClassName.length());
        }
        return shortClassName + "." + child;
    }*/

    /** Returns the expected XML element name for the given child of the given Segment */
    private String makeElementName(Segment s, int child) {
        return s.getName() + "." + child;
    }

    /** Returns the expected XML element name for the given child of the given Composite */
    private String makeElementName(Composite composite, int child) {
        return composite.getName() + "." + child;
    }

    /**
     * Populates the given Element with data from the given Type, by inserting
     * Elements corresponding to the Type's components and values.  Returns true if 
     * the given type contains a value (i.e. for Primitives, if getValue() doesn't 
     * return null, and for Composites, if at least one underlying Primitive doesn't 
     * return null).
     */
    private boolean encode(Type datatypeObject, Element datatypeElement) throws DataTypeException {
        boolean hasData = false;
        if (datatypeObject instanceof Varies) {
            hasData = encodeVaries((Varies) datatypeObject, datatypeElement);
        }
        else if (datatypeObject instanceof Primitive) {
            hasData = encodePrimitive((Primitive) datatypeObject, datatypeElement);
        }
        else if (datatypeObject instanceof Composite) {
            hasData = encodeComposite((Composite) datatypeObject, datatypeElement);
        }
        return hasData;
    }

    /**
     * Encodes a Varies type by extracting it's data field and encoding that.  Returns true 
     * if the data field (or one of its components) contains a value.  
     */
    private boolean encodeVaries(Varies datatypeObject, Element datatypeElement) throws DataTypeException {
        boolean hasData = false;
        if (datatypeObject.getData() != null) {
            hasData = encode(datatypeObject.getData(), datatypeElement);
        }
        return hasData;
    }

    /** 
     * Encodes a Primitive in XML by adding it's value as a child of the given Element.  
     * Returns true if the given Primitive contains a value.  
     */
    private boolean encodePrimitive(Primitive datatypeObject, Element datatypeElement) throws DataTypeException {
        boolean hasValue = false;
        if (datatypeObject.getValue() != null && !datatypeObject.getValue().equals(""))
            hasValue = true;

        Text t = datatypeElement.getOwnerDocument().createTextNode(datatypeObject.getValue());
        if (hasValue) {
            try {
                datatypeElement.appendChild(t);
            }
            catch (DOMException e) {
                throw new DataTypeException("DOMException encoding Primitive: ", e);
            }
        }
        return hasValue;
    }

    /**
     * Encodes a Composite in XML by looping through it's components, creating new 
     * children for each of them (with the appropriate names) and populating them by 
     * calling encode(Type, Element) using these children.  Returns true if at least 
     * one component contains a value.  
     */
    private boolean encodeComposite(Composite datatypeObject, Element datatypeElement) throws DataTypeException {
        Type[] components = datatypeObject.getComponents();
        boolean hasValue = false;
        for (int i = 0; i < components.length; i++) {
            String name = makeElementName(datatypeObject, i + 1);
            Element newNode = datatypeElement.getOwnerDocument().createElement(name);
            boolean componentHasValue = encode(components[i], newNode);
            if (componentHasValue) {
                try {
                    datatypeElement.appendChild(newNode);
                }
                catch (DOMException e) {
                    throw new DataTypeException("DOMException encoding Composite: ", e);
                }
                hasValue = true;
            }
        }
        return hasValue;
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
     * avoiding the condition that caused the original error.</p>
     */
    public Segment getCriticalResponseData(String message) throws HL7Exception {
        String version = getVersion(message);
        Segment criticalData = Parser.makeControlMSH(version, getFactory());

        Terser.set(criticalData, 1, 0, 1, 1, parseLeaf(message, "MSH.1", 0));
        Terser.set(criticalData, 2, 0, 1, 1, parseLeaf(message, "MSH.2", 0));
        Terser.set(criticalData, 10, 0, 1, 1, parseLeaf(message, "MSH.10", 0));
        String procID = parseLeaf(message, "MSH.11", 0);
        if (procID == null || procID.length() == 0) {
            procID = parseLeaf(message, "PT.1", message.indexOf("MSH.11"));
            //this field is a composite in later versions
        }
        Terser.set(criticalData, 11, 0, 1, 1, procID);

        return criticalData;
    }

    /**
     * For response messages, returns the value of MSA-2 (the message ID of the message
     * sent by the sending system).  This value may be needed prior to main message parsing,
     * so that (particularly in a multi-threaded scenario) the message can be routed to
     * the thread that sent the request.  We need this information first so that any
     * parse exceptions are thrown to the correct thread.  Implementers of Parsers should
     * take care to make the implementation of this method very fast and robust.
     * Returns null if MSA-2 can not be found (e.g. if the message is not a
     * response message).  Trims whitespace from around the MSA-2 field.  
     */
    public String getAckID(String message) {
        String ackID = null;
        try {
            ackID = parseLeaf(message, "msa.2", 0).trim();
        }
        catch (HL7Exception e) { /* OK ... assume it isn't a response message */
        }
        return ackID;
    }

    public String getVersion(String message) throws HL7Exception {
        String version = parseLeaf(message, "MSH.12", 0);
        if (version == null || version.trim().length() == 0) {
            version = parseLeaf(message, "VID.1", message.indexOf("MSH.12"));
        }
        return version;
    }

    /**
     * Attempts to retrieve the value of a leaf tag without using DOM or SAX.  
     * This method searches the given message string for the given tag name, and returns 
     * everything after the given tag and before the start of the next tag.  Whitespace
     * is stripped.  This is intended only for lead nodes, as the value is considered to 
     * end at the start of the next tag, regardless of whether it is the matching end 
     * tag or some other nested tag.  
     * @param message a string message in XML form
     * @param tagName the name of the XML tag, e.g. "MSA.2"
     * @param startAt the character location at which to start searching
     * @throws HL7Exception if the tag can not be found
     */
    protected String parseLeaf(String message, String tagName, int startAt) throws HL7Exception {
        String value = null;
        
        int  tagStart = -1;
        
        String tagPat   = "\\.";
        String regex    = "";
        
        try {
            // Replace any dots with an escaped dot in the tagName.
            String tagRegex = Pattern.compile( tagPat ).matcher( tagName ).replaceAll( "\\." );
            
            // Create a regex to match element names, including those that have a namespace prefix
            regex = "<([0-9a-zA-Z][-\\w]*:)?(" + tagRegex + "|" + tagRegex.toUpperCase() + ")";
            
            Matcher matcher = Pattern.compile( regex ).matcher( message );
                
            if( matcher.find( startAt ) ) {
                tagStart = matcher.start();
            }
        }
        catch( PatternSyntaxException pse ) {
            throw new HL7Exception(
                    "Invalid RegEx Pattern: \"" + tagPat + "\" or \"" + regex + "\"",
                    HL7Exception.APPLICATION_INTERNAL_ERROR,
                    pse);
        }       
        
        int valStart = message.indexOf(">", tagStart) + 1;
        int valEnd = message.indexOf("<", valStart);

        if (tagStart >= 0 && valEnd >= valStart) {
            value = message.substring(valStart, valEnd);
        }
        else {
            throw new HL7Exception(
                "Couldn't find "
                    + tagName
                    + " in message beginning: "
                    + message.substring(0, Math.min(150, message.length())),
                HL7Exception.REQUIRED_FIELD_MISSING);
        }

        return value;
    }

    /** Test harness */
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: XMLParser pipe_encoded_file");
            System.exit(1);
        }

        //read and parse message from file 
        try {
            PipeParser parser = new PipeParser();
            File messageFile = new File(args[0]);
            long fileLength = messageFile.length();
            FileReader r = new FileReader(messageFile);
            char[] cbuf = new char[(int) fileLength];
            System.out.println("Reading message file ... " + r.read(cbuf) + " of " + fileLength + " chars");
            r.close();
            String messString = String.valueOf(cbuf);
            Message mess = parser.parse(messString);
            System.out.println("Got message of type " + mess.getClass().getName());

            ca.uhn.hl7v2.parser.XMLParser xp = new XMLParser() {
                public Message parseDocument(Document XMLMessage, String version) throws HL7Exception {
                    return null;
                }
                public Document encodeDocument(Message source) throws HL7Exception {
                    return null;
                }
                public String getVersion(String message) throws HL7Exception {
                    return null;
                }
            };

            //loop through segment children of message, encode, print to console
            String[] structNames = mess.getNames();
            for (int i = 0; i < structNames.length; i++) {
                Structure[] reps = mess.getAll(structNames[i]);
                for (int j = 0; j < reps.length; j++) {
                    if (Segment.class.isAssignableFrom(reps[j].getClass())) { //ignore groups
                        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document doc = docBuilder.newDocument(); //new doc for each segment
                        Element root = doc.createElement(reps[j].getClass().getName());
                        doc.appendChild(root);
                        xp.encode((Segment) reps[j], root);
                        StringWriter out = new StringWriter();
                        XMLSerializer ser = new XMLSerializer(out, null); //default output format
                        ser.serialize(doc);
                        System.out.println("Segment " + reps[j].getClass().getName() + ": \r\n" + out.toString());

                        Class[] segmentConstructTypes = { Message.class };
                        Object[] segmentConstructArgs = { null };
                        Segment s =
                            (Segment) reps[j].getClass().getConstructor(segmentConstructTypes).newInstance(
                                segmentConstructArgs);
                        xp.parse(s, root);
                        Document doc2 = docBuilder.newDocument();
                        Element root2 = doc2.createElement(s.getClass().getName());
                        doc2.appendChild(root2);
                        xp.encode(s, root2);
                        StringWriter out2 = new StringWriter();
                        ser = new XMLSerializer(out2, null); //default output format
                        ser.serialize(doc2);
                        if (out2.toString().equals(out.toString())) {
                            System.out.println("Re-encode OK");
                        }
                        else {
                            System.out.println(
                                "Warning: XML different after parse and re-encode: \r\n" + out2.toString());
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
