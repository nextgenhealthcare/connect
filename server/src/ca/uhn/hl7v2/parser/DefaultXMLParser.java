package ca.uhn.hl7v2.parser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.log.HapiLog;
import ca.uhn.log.HapiLogFactory;

/**
 * <p>A default XMLParser.  This class assigns segment elements (in an XML-encoded message) 
 * to Segment objects (in a Message object) using the name of a segment and the names 
 * of any groups in which the segment is nested.  The names of group classes must correspond
 * to the names of group elements (they must be identical except that a dot in the element 
 * name, following the message name, is replaced with an underscore, in order to consitute a 
 * valid class name). </p>
 * <p>At the time of writing, the group names in the XML spec are changing.  Many of the group 
 * names have been automatically generated based on the group contents.  However, these automatic 
 * names are gradually being replaced with manually assigned names.  This process is expected to 
 * be complete by November 2002.  As a result, mismatches are likely.  Messages could be  
 * transformed prior to parsing (using XSLT) as a work-around.  Alternatively the group class names 
 * could be changed to reflect updates in the XML spec.  Ultimately, HAPI group classes will be 
 * changed to correspond with the official group names, once these are all assigned.  </p>
 * @author Bryan Tripp
 */
public class DefaultXMLParser extends XMLParser {

    private static final HapiLog log = HapiLogFactory.getHapiLog(DefaultXMLParser.class);

    /** Creates a new instance of DefaultXMLParser */
    public DefaultXMLParser() {
    }

    /**
     * <p>Creates an XML Document that corresponds to the given Message object. </p>
     * <p>If you are implementing this method, you should create an XML Document, and insert XML Elements
     * into it that correspond to the groups and segments that belong to the message type that your subclass
     * of XMLParser supports.  Then, for each segment in the message, call the method
     * <code>encode(Segment segmentObject, Element segmentElement)</code> using the Element for
     * that segment and the corresponding Segment object from the given Message.</p>
     */
    public Document encodeDocument(Message source) throws HL7Exception {
        String messageClassName = source.getClass().getName();
        String messageName = messageClassName.substring(messageClassName.lastIndexOf('.') + 1);
        //Fix for parsing generic messages
        if (messageName.indexOf('$') > -1){
        	messageName = messageName.substring(0,messageName.indexOf('$'));
        }
        org.w3c.dom.Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement(messageName);
            doc.appendChild(root);
        }
        catch (Exception e) {
            throw new HL7Exception(
                "Can't create XML document - " + e.getClass().getName(),
                HL7Exception.APPLICATION_INTERNAL_ERROR,
                e);
        }
        encode(source, doc.getDocumentElement());
        return doc;
    }

    /**
     * Copies data from a group object into the corresponding group element, creating any 
     * necessary child nodes.  
     */
    private void encode(ca.uhn.hl7v2.model.Group groupObject, org.w3c.dom.Element groupElement) throws HL7Exception {
        String[] childNames = groupObject.getNames();
        String messageName = groupObject.getMessage().getName();
        
        try {
            for (int i = 0; i < childNames.length; i++) {
                Structure[] reps = groupObject.getAll(childNames[i]);
                for (int j = 0; j < reps.length; j++) {
                    Element childElement =
                        groupElement.getOwnerDocument().createElement(makeGroupElementName(messageName, childNames[i]));
                    groupElement.appendChild(childElement);
                    if (reps[j] instanceof Group) {
                        encode((Group) reps[j], childElement);
                    }
                    else if (reps[j] instanceof Segment) {
                        encode((Segment) reps[j], childElement);
                    }
                }
            }
        }
        catch (DOMException e) {
            throw new HL7Exception(
                "Can't encode group " + groupObject.getClass().getName(),
                HL7Exception.APPLICATION_INTERNAL_ERROR,
                e);
        }
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
     *     is not supported by this parser.
     */
    public Message parseDocument(org.w3c.dom.Document XMLMessage, String version) throws HL7Exception {
        String messageName = XMLMessage.getDocumentElement().getLocalName();
        Message message = instantiateMessage(messageName, version, true);
        parse(message, XMLMessage.getDocumentElement());
        return message;
    }

    /**
     * Populates the given group object with data from the given group element, ignoring 
     * any unrecognized nodes.  
     */
    private void parse(ca.uhn.hl7v2.model.Group groupObject, org.w3c.dom.Element groupElement) throws HL7Exception {
        String[] childNames = groupObject.getNames();
        String messageName = groupObject.getMessage().getName();
        
        NodeList allChildNodes = groupElement.getChildNodes();
        ArrayList unparsedElementList = new ArrayList();
        for (int i = 0; i < allChildNodes.getLength(); i++) {
            Node node = allChildNodes.item(i);
            String name = node.getLocalName();
            if (node.getNodeType() == Node.ELEMENT_NODE && !unparsedElementList.contains(name)) {
                unparsedElementList.add(name);                
            }
        }
        
        //we're not too fussy about order here (all occurances get parsed as repetitions) ... 
        for (int i = 0; i < childNames.length; i++) {
            unparsedElementList.remove(childNames[i]);
            parseReps(groupElement, groupObject, messageName, childNames[i], childNames[i]);
        }
        
        for (int i = 0; i < unparsedElementList.size(); i++) {
            String segName = (String) unparsedElementList.get(i);            
            String segIndexName = groupObject.addNonstandardSegment(segName);
            parseReps(groupElement, groupObject, messageName, segName, segIndexName);
        }
    }
    
    //param childIndexName may have an integer on the end if >1 sibling with same name (e.g. NTE2) 
    private void parseReps(Element groupElement, Group groupObject, 
            String messageName, String childName, String childIndexName) throws HL7Exception {

        List reps = getChildElementsByTagName(groupElement, makeGroupElementName(messageName, childName));
        log.debug("# of elements matching " 
            + makeGroupElementName(messageName, childName) + ": " + reps.size());

		if (groupObject.isRepeating(childIndexName)) {
			for (int i = 0; i < reps.size(); i++) {
				parseRep((Element) reps.get(i), groupObject.get(childIndexName, i));
			}        			        
		} else {
			if (reps.size() > 0) {
				parseRep((Element) reps.get(0), groupObject.get(childIndexName, 0));				
			}

			if (reps.size() > 1) {			
				String newIndexName = groupObject.addNonstandardSegment(childName);			
				for (int i = 1; i < reps.size(); i++) {
					parseRep((Element) reps.get(i), groupObject.get(newIndexName, i-1));
				}        			        			
			}
		}
    }
    
    private void parseRep(Element theElem, Structure theObj) throws HL7Exception {
		if (theObj instanceof Group) {
			parse((Group) theObj, theElem);
		}
		else if (theObj instanceof Segment) {
			parse((Segment) theObj, theElem);
		}                
		log.debug("Parsed element: " + theElem.getLocalName()); 
    }
    
    //includes direct children only
    private List getChildElementsByTagName(Element theElement, String theName) {
    	List result = new ArrayList(10);
    	NodeList children = theElement.getChildNodes();
    	
    	for (int i = 0; i < children.getLength(); i++) {
    		Node child = children.item(i);
    		if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(theName)) {
    			result.add(child);
    		}
    	}
    	
    	return result; 
    }
    
    /** 
     * Given the name of a group element in an XML message, returns the corresponding 
     * group class name.  This name is identical except in order to be a valid class 
     * name, the dot character immediately following the message name is replaced with 
     * an underscore.  For example, there is a group element called ADT_A01.INSURANCE and the 
     * corresponding group Class is called ADT_A01_INSURANCE. 
     */
//    protected static String makeGroupClassName(String elementName) {
//        return elementName.replace('.', '_');
//    }

    /** 
     * Given the name of a message and a Group class, returns the corresponding group element name in an 
     * XML-encoded message.  This is the message name and group name separated by a dot. For example, 
     * ADT_A01.INSURANCE.
     * 
     * If it looks like a segment name (i.e. has 3 characters), no change is made. 
     */
    protected static String makeGroupElementName(String messageName, String className) {
        String ret = null;

        if (className.length() > 4) {
            StringBuffer elementName = new StringBuffer();
            elementName.append(messageName);
            elementName.append('.');
            elementName.append(className);
            ret = elementName.toString();
        } else if (className.length() == 4) {
            ret = className.substring(0,3);
        } else {
            ret = className;
        }
        
        return ret;
    }

    /** Test harness */
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: DefaultXMLParser pipe_encoded_file");
            System.exit(1);
        }

        //read and parse message from file 
        try {
            File messageFile = new File(args[0]);
            long fileLength = messageFile.length();
            FileReader r = new FileReader(messageFile);
            char[] cbuf = new char[(int) fileLength];
            System.out.println("Reading message file ... " + r.read(cbuf) + " of " + fileLength + " chars");
            r.close();
            String messString = String.valueOf(cbuf);

            Parser inParser = null;
            Parser outParser = null;
            PipeParser pp = new PipeParser();
            ca.uhn.hl7v2.parser.XMLParser xp = new DefaultXMLParser();
            System.out.println("Encoding: " + pp.getEncoding(messString));
            if (pp.getEncoding(messString) != null) {
                inParser = pp;
                outParser = xp;
            }
            else if (xp.getEncoding(messString) != null) {
                inParser = xp;
                outParser = pp;
            }

            Message mess = inParser.parse(messString);
            System.out.println("Got message of type " + mess.getClass().getName());

            String otherEncoding = outParser.encode(mess);
            System.out.println(otherEncoding);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
