package com.webreach.mirth.model.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.ContentHandlerAdapter;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.SAXWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class    DICOMSerializer implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
    public boolean validationError = false;
    public String rawData;
    private ArrayList<String> pixelData;

	public static Map getDefaultProperties() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}
    
    public DICOMSerializer(Map DICOMProperties){
        if (DICOMProperties == null) { 
		}
    }

	private static String convertNonPrintableCharacters(String delimiter) {
		return delimiter.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
	}

	public DICOMSerializer() {
	}

    public ArrayList<String> getPixelData() {
        return pixelData;
    }
    
    public String fromXML(String source) throws SerializerException {
        if(source == null || source.length()==0){
            return "";
        }
        try {
            // 1. reparse the xml to Mirth format
            Document document = getDocument(source);
            Element element = document.getDocumentElement();
            Node node = element.getChildNodes().item(0);
            // change back to <attr> tag for all tags under <dicom> tag
            while(node != null){
                renameNode(document, node, false);
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
                            renameNode(document,nodeItem, false);
                        }
                    }
                }
            }      
            // parse document into dicomObject
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p = f.newSAXParser();    
            DicomObject dicomObject = new BasicDicomObject();            
            ContentHandlerAdapter ch = new ContentHandlerAdapter(dicomObject);            
            p.parse(new InputSource(new ByteArrayInputStream(new DocumentSerializer().toXML(document).trim().getBytes("UTF8"))),ch);

            byte[] temp = readDicomObj(dicomObject);
            return encodeMessage(temp);
        } 
        catch (Exception e) {
            e.printStackTrace();
            throw new SerializerException(e.getMessage());
		}
    }

	public String toXML(String source) throws SerializerException  {        
        try {
            // 1. Decode source
            byte[] binarySource = decodeMessage(source);

            DicomObject dcmObj = getDicomObjFromByteArray(binarySource);             
            // read in header and pixel data
            readPixelData(dcmObj);
            byte[] decodedMessage = readDicomObj(dcmObj);
            rawData = encodeMessage(decodedMessage); 
            return convertToXML(decodedMessage);
        } catch (Exception e) {
            throw new SerializerException(e.getMessage());
        }
	}
    public String toXML(File tempDCMFile) throws SerializerException {
        try {
            // Encode it before transforming it
            return toXML(encodeMessage(getBytesFromFile(tempDCMFile)));
        } catch (Exception e) {
            //throw new SerializerException(e.getMessage());
            return "Invalid DICOM Message";
        }
    }
    
    private Map<String, String> getMetadata(String sourceMessage) {
		DocumentSerializer docSerializer = new DocumentSerializer();
		docSerializer.setPreserveSpace(true);
		Document document = docSerializer.fromXML(sourceMessage);
		return getMetadataFromDocument(document);
	}

	public Map<String, String> getMetadataFromDocument(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		String version = "";
		map.put("version", version);
		String event = "DICOM";
		map.put("type", event);
		String sendingFacility = "dicom";
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
    private static String decodeTagNames(String input) throws SerializerException {
        try {
            Document document = getDocument(input);
            NodeList nodeList = document.getElementsByTagName("attr");
            for(int i = 0; i < nodeList.getLength();i++){
                Node node = nodeList.item(i);
                renameNode(document, node, true); 
            }
            return new DocumentSerializer().toXML(document);
        }
        catch(Exception e){
            throw new SerializerException(e.getMessage());
        }
    }
    
    private static void renameNode(Document document,Node node, boolean toTagName){
        if(toTagName){
            if(node.getNodeName().equals("attr")){
                Node tagAttr = node.getAttributes().getNamedItem("tag");
                String tagDesc = tagAttr.getNodeValue();
                try {
                    if(!tagDesc.equals("?")) 
                        document.renameNode(node,null,"tag"+tagDesc);  
                }
                catch(DOMException e){
                    e.printStackTrace();
                }            
            }
        }
        else {
            NamedNodeMap attr = node.getAttributes();
            if(attr == null) {
                return;
            }
            Node tagAttr = attr.getNamedItem("tag");
            if(tagAttr != null) {
                String tag = tagAttr.getNodeValue();
                String tagDesc = "tag"+tag; 
                try {
                    if(!tagDesc.equals("?") && node.getNodeName().equals(tagDesc))  {
                        document.renameNode(node,null,"attr");
                    }
                }
                catch(DOMException e){
                    e.printStackTrace();
                }
            }          
        }
    }
    
    private static Document getDocument(String source) throws SerializerException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
  
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(source))); 
        }
        catch(Exception e){
            throw new SerializerException(e.getMessage());
        }        
    }

    private static String convertToXML(byte[] temp) throws SerializerException {
      
        StringWriter xmlOutput = new StringWriter();
        BasicDicomObject dicomObject = new BasicDicomObject();
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(temp); 
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
                throw new SerializerException(e.getMessage());
            }
            finally {
                dis.close();
            }
        }
        catch(Exception e){
            throw new SerializerException(e.getMessage());
        }
        //return xmlOutput.toString();
        return decodeTagNames(xmlOutput.toString());
    }
    public static String removeInvalidCharXML(String tag){
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
		int numRead;
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
    
    private void readPixelData(DicomObject dcmObj){
        DicomElement dicomElement = dcmObj.get(Tag.PixelData);
        if(dicomElement != null) {
            if(dicomElement.hasItems()){
                // each one is a attachment
                int count = dicomElement.countItems();
                pixelData = new ArrayList(count);
                for (int i = 0; i < count; i++) {
                    byte[] image = dicomElement.getFragment(i);
                    pixelData.add(encodeMessage(image));
                }
            }
            else {
                pixelData = new ArrayList(1);
                pixelData.add(encodeMessage(dicomElement.getBytes()));
            }
        }
        dcmObj.remove(Tag.PixelData);

    }
    
    public static byte[] readDicomObj(DicomObject dcmObj) throws SerializerException {
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
            throw new SerializerException(e.getMessage());
        }
        finally {
            try {
                if(dos != null)
                    dos.close();
            }
            catch (IOException ignore) {
            }
        }        
    }

    public static String mergeHeaderPixelData(byte[] header, ArrayList<byte[]> images) throws SerializerException {
        // 1. read in header
        DicomObject dcmObj = getDicomObjFromByteArray(header);
        // 2. Add pixel data to DicomObject
        if(images != null && !images.isEmpty()){
            if(images.size() > 1){
                DicomElement dicomElement = dcmObj.putFragments(Tag.PixelData, null, dcmObj.bigEndian(), images.size());
                for(byte[] image : images){
                    dicomElement.addFragment(image);
                }
                dcmObj.add(dicomElement);
            }
            else {
                dcmObj.putBytes(Tag.PixelData,VR.OW, images.get(0));
            }
        }
        // get byteArray from dicomObject
        return encodeMessage(readDicomObj(dcmObj));  
    }    
    private static DicomObject getDicomObjFromByteArray(byte[] dicomByteArray) throws SerializerException {
        // 1. read in header
        DicomObject dcmObj = new BasicDicomObject();
        DicomInputStream din = null;
        try {
            din = new DicomInputStream(new ByteArrayInputStream(dicomByteArray));
            din.readDicomObject(dcmObj, -1);
        }
        catch (IOException e) {
            throw new SerializerException(e.getMessage());
        }
        finally {
            try {
                if(din != null)
                    din.close();
            }
            catch (IOException ignore) {
            }
        }
        return dcmObj;
    }
    private static String encodeMessage(byte[] message){
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(message);
    }
    private static byte[] decodeMessage(String message) throws SerializerException{
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            return decoder.decodeBuffer(message);
        }
        catch(IOException io){
            throw new SerializerException(io.getMessage());
        }
    }    
}
