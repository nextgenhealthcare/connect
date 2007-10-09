package com.webreach.mirth.model.converters;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.dcm4che2.data.*;
import org.dcm4che2.io.*;
import org.dcm4che2.tool.dcm2xml.Dcm2Xml;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.parsers.*;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import java.util.*;
import java.io.*;

import com.webreach.mirth.model.dicom.DICOMReference;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;


/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 6, 2007
 * Time: 11:27:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMSerializer implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
    public boolean validationError = false;
    private boolean includeGroupLength = false;
    public String rawData;
    public ArrayList pixelData;
    
    public DICOMSerializer(Map DICOMProperties){
        if (DICOMProperties == null) { 
			return;
		}
		if (DICOMProperties.get("includeGroupLength") != null) {
            String groupLength = convertNonPrintableCharacters((String) DICOMProperties.get("includeGroupLength"));
            if(groupLength.equals("false")){
                this.includeGroupLength = false;
            }
            else {
                this.includeGroupLength = true;
            }
        } 
    }

	private String convertNonPrintableCharacters(String delimiter) {
		return delimiter.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
	}

	public DICOMSerializer() {
	}

	public String fromXML(String source) throws SerializerException {
        if(source == null || source.equals("")){
            return "";
        }
        try {
            // 1. reparse the xml to Mirth format
            DicomObject dicomObject = new BasicDicomObject();
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p = f.newSAXParser();
            ContentHandlerAdapter ch = new ContentHandlerAdapter(dicomObject);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(source)));
                Element element = document.getDocumentElement();
                Node node = element.getChildNodes().item(0);
                // change back to <attr> tag for all tags under <dicom> tag
                while(node != null){
                    NamedNodeMap attr = node.getAttributes();
                    if(attr == null) {
                        node = node.getNextSibling();
                        continue;
                    }
                    Node tagAttr = attr.getNamedItem("tag");
                    //System.out.println("tag (value): " + tagAttr.getNodeValue());
                    if(tagAttr != null) {
                        String tag = tagAttr.getNodeValue();
                        String tagDesc = DICOMReference.getInstance().getDescription(tag,null);
                        tagDesc = removeInvalidCharXML(tagDesc);
                        //System.out.println("tag: " + tagDesc);
                        try {
                            if(!tagDesc.equals("?"))  {
                                if(node.getNodeName().equals(tagDesc)){
                                    document.renameNode(node,null,"attr");
                                }
                            }
                        }
                        catch(DOMException e){
                            e.printStackTrace();
                        }
                    }
                    node = node.getNextSibling();
                }
                NodeList items = document.getElementsByTagName("item");
                // change back to <attr> tag for all tags under <item> tags
                if(items != null){
                    for(int i=0;i<items.getLength();i++){
                        Node itemNode = items.item(i);
                        if(itemNode.getChildNodes() != null){
                            NodeList itemNodes = itemNode.getChildNodes();
                            for(int j=0;j<itemNodes.getLength();j++){
                                Node nodeItem = itemNodes.item(j);
                                NamedNodeMap attr = nodeItem.getAttributes();
                                if(attr == null) {
                                    continue;
                                }
                                Node tagAttr = attr.getNamedItem("tag");
                                //System.out.println("tag (value): " + tagAttr.getNodeValue());
                                if(tagAttr != null) {
                                    String tag = tagAttr.getNodeValue();
                                    String tagDesc = DICOMReference.getInstance().getDescription(tag,null);
                                    tagDesc = removeInvalidCharXML(tagDesc);
                                    //System.out.println("tag: " + tagDesc);
                                    try {
                                        if(!tagDesc.equals("?"))  {
                                            if(nodeItem.getNodeName().equals(tagDesc)){
                                                document.renameNode(nodeItem,null,"attr");
                                            }
                                        }
                                    }
                                    catch(DOMException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }       
                p.parse(new InputSource(new ByteArrayInputStream(new DocumentSerializer().toXML(document).trim().getBytes("UTF8"))),ch);
               // p.parse(tempXML,ch);

                byte[] temp = readDicomObj(dicomObject);
                
                BASE64Encoder encoder = new BASE64Encoder();
                String encodedMessage = encoder.encode(temp);
                return encodedMessage;
            }
            catch(ParserConfigurationException e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SerializerException(e.getMessage());
		}
        return new String();
    }

	public String toXML(String source)   {        
        try {
            // 1. Decode source
            byte[] temp = null;
            BASE64Decoder decoder = new BASE64Decoder();
            temp = decoder.decodeBuffer(source);
            // 2. put data into a temp file
            String test = new String(temp);

            DicomObject dcmObj = getDicomObjFromByteArray(temp);             
            // read in header and pixel data
            readPixelData(dcmObj);
            readRawDataFromDicomObject(dcmObj);
            String xmlData = convertToXML(temp);
            // get Header data
            rawData = fromXML(xmlData);

            return xmlData;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return new String();
	}
    public String toXML(File tempDCMFile) throws SerializerException {
        try {
            // Encode it before transforming it
            BASE64Encoder encoder = new BASE64Encoder();
            return toXML(encoder.encode(getBytesFromFile(tempDCMFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String();
    }
    
    private Map<String, String> getMetadata(String sourceMessage) throws SerializerException {
		DocumentSerializer docSerializer = new DocumentSerializer();
		docSerializer.setPreserveSpace(true);
		Document document = docSerializer.fromXML(sourceMessage);
		return getMetadataFromDocument(document);
	}

	public Map<String, String> getMetadataFromDocument(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		String sendingFacility = "dicom";
		String event = "DICOM";
		String version = "";
		map.put("version", version);
		map.put("type", event);
		map.put("source", sendingFacility);
		return map;
	}

	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
		String DICOMXML = fromXML(source);
		return getMetadata(DICOMXML);
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		return getMetadata(xmlSource);
	}
    private String decodeTagNames(String input){
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document;
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(new InputSource(new StringReader(input)));
                NodeList nodeList = document.getElementsByTagName("attr");
                for(int i = 0; i < nodeList.getLength();i++){
                    Node node = nodeList.item(i);
                    if(node.getNodeName().equals("attr")){
                        Node tagAttr = node.getAttributes().getNamedItem("tag");
                        String tag = tagAttr.getNodeValue();
                        String tagDesc = DICOMReference.getInstance().getDescription(tag,null);
                        tagDesc = removeInvalidCharXML(tagDesc);
                        try {
                            if(!tagDesc.equals("?")) 
                                document.renameNode(node,null,tagDesc);  
                        }
                        catch(DOMException e){
                            e.printStackTrace();
                        }
                        if(node.getNodeName() != null && node.getNodeName().equals("PixelData")){
                            node.getParentNode().removeChild(node);
                        }
                    }
                }
                return new DocumentSerializer().toXML(document);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        return new String();
    }

    private String convertToXML(byte[] temp){
      
        StringWriter xmlOutput = new StringWriter();
        BasicDicomObject dicomObject = new BasicDicomObject();
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(temp); // decoder.decodeBuffer(rawData));
            DicomInputStream dis = new DicomInputStream(bis); 
            try {
                SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
                TransformerHandler th = tf.newTransformerHandler();
                th.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
                th.setResult(new StreamResult(xmlOutput));
                final SAXWriter writer = new SAXWriter(th, null);
                dis.setHandler(writer);
                dis.readDicomObject(dicomObject, -1);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                dis.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //return xmlOutput.toString();
        return decodeTagNames(xmlOutput.toString());
    }
    public String removeInvalidCharXML(String tag){
        tag = tag.replaceAll(" ", "");
        tag = tag.replaceAll("'", "");
        tag = tag.replaceAll("\\(","");
        tag = tag.replaceAll("\\)","");
        tag = tag.replaceAll("/","");
        tag = tag.replaceAll("&","");
        return tag;
    }
	// Returns the contents of the file in a byte array.
	private static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
    
    public void readPixelData(DicomObject dcmObj){
        DicomElement dicomElement = dcmObj.get(Tag.PixelData);
        BASE64Encoder encoder = new BASE64Encoder();
        if(dicomElement != null) {
            if(dicomElement.hasItems()){
                // each one is a attachment
                int count = dicomElement.countItems();
                pixelData = new ArrayList(count);
                for (int i = 0; i < count; i++) {
                    byte[] image = dicomElement.getFragment(i);
                    pixelData.add(encoder.encode(image));
                }
            }
            else {
                pixelData = new ArrayList(1);
                pixelData.add(encoder.encode(dicomElement.getBytes()));
            }
        }
        dcmObj.remove(Tag.PixelData);

    }
    
    public void readRawDataFromDicomObject(DicomObject dcmObj) {
        BASE64Encoder encoder = new BASE64Encoder();
        rawData = encoder.encode(readDicomObj(dcmObj));
    }
    
    public static byte[] readDicomObj(DicomObject dcmObj){
        BasicDicomObject bDcmObj = (BasicDicomObject) dcmObj;
        DicomOutputStream dos = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            dos = new DicomOutputStream(bos);
            if(bDcmObj.fileMetaInfo().isEmpty()) {
                // Create ACR/NEMA Dump
                String tsuid = TransferSyntax.ImplicitVRLittleEndian.uid();
                dos.writeDataset(bDcmObj, TransferSyntax.valueOf(tsuid));
            }
            else {
                // Create DICOM File
                dos.writeDicomFile(bDcmObj);    
            }
            return bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "".getBytes();
        }
        finally {
            try {
                dos.close();
            }
            catch (IOException ignore) {
            }
        }        
    }
    
    
    public static String mergeHeaderPixelData(byte[] header, byte[] pixelData){
        
        // 1. read in header
        DicomObject dcmObj = getDicomObjFromByteArray(header);
        // 2. Add pixel data to DicomObject
        if(pixelData != null){
            dcmObj.putBytes(Tag.PixelData,VR.OW,pixelData);
        }
        // get byteArray from dicomObject
        byte[] temp = readDicomObj(dcmObj);
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(temp);  
    }
    public static String mergeHeaderPixelData(byte[] header, ArrayList images){
        
        // 1. read in header
        DicomObject dcmObj = getDicomObjFromByteArray(header);
        // 2. Add pixel data to DicomObject
        if(images != null && !images.isEmpty()){
            DicomElement dicomElement = dcmObj.putFragments(Tag.PixelData, null, dcmObj.bigEndian(), images.size());
            Iterator i = images.iterator();
            while(i.hasNext()){
                byte[] image = (byte[]) i.next();
                dicomElement.addFragment(image);
            }
            dcmObj.add(dicomElement);
        }
        // get byteArray from dicomObject
        byte[] temp = readDicomObj(dcmObj);
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(temp);  
    }    
    public static DicomObject getDicomObjFromByteArray(byte[] dicomByteArray){
        // 1. read in header
        DicomObject dcmObj = new BasicDicomObject();
        DicomInputStream din = null;
        try {
            din = new DicomInputStream(new ByteArrayInputStream(dicomByteArray));
            din.readDicomObject(dcmObj, -1);
        }
        catch (IOException e) {
            e.printStackTrace();
            return dcmObj;
        }
        finally {
            try {
                din.close();
            }
            catch (IOException ignore) {
            }
        }
        return dcmObj;
    }
}
