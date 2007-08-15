package com.webreach.mirth.model.converters;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import java.util.Map;
import java.util.HashMap;
import java.io.*;


import com.webreach.mirth.model.dicom.DICOMReference;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 6, 2007
 * Time: 11:27:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMSerializer implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
	private String segmentDelim = "\u001E";
	private String groupDelim = "\u001D";
	private String fieldDelim = "\u001C";
	private boolean includePixelData = true;
    public boolean validationError = false;

    public DICOMSerializer(Map DICOMProperties){
		if (DICOMProperties == null) {
			return;
		}
		if (DICOMProperties.get("includePixelData") != null) {
            String pixelData = convertNonPrintableCharacters((String) DICOMProperties.get("includePixelData"));
            if(pixelData.equals("yes")){
                this.includePixelData = true;
            }
            else {
                this.includePixelData = false;
            }
        }
    }

	private String convertNonPrintableCharacters(String delimiter) {
		return delimiter.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
	}

	public DICOMSerializer() {
	}

	public String fromXML(String source) throws SerializerException {
        try {
            DicomObject dicomObject = new BasicDicomObject();
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p = f.newSAXParser();
            ContentHandlerAdapter ch = new ContentHandlerAdapter(dicomObject);

            // reparse the xml to Mirth format
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(source)));
                Element element = document.getDocumentElement();
                Node node = element.getChildNodes().item(0);
                while(node != null){
                    NamedNodeMap attr = node.getAttributes();
                    Node tagAttr = attr.getNamedItem("tag");
                    if(tagAttr != null) {
                        String tag = tagAttr.getNodeValue();
                        String tagDesc = DICOMReference.getInstance().getDescription(tag,null);
                        tagDesc = tagDesc.replaceAll(" ", "");
                        tagDesc = tagDesc.replaceAll("'", "");
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
                p.parse(new InputSource(new StringReader(new DocumentSerializer().toXML(document))),ch);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DicomOutputStream dos = new DicomOutputStream(bos);
                dos.writeDicomObject(dicomObject);
                dos.close();
                return bos.toString();                
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

	public String toXML(String source) throws SerializerException {
		try {
            ByteArrayInputStream bis = new ByteArrayInputStream(source.getBytes());
            DicomInputStream dis = new DicomInputStream(bis);
            return convertToXML(dis);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return new String();
	}
    public String toXML(File tempDCMFile) throws SerializerException {
        try {
            DicomInputStream dis = new DicomInputStream(tempDCMFile);
            return convertToXML(dis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String();
    }


    public String getSegmentDelim() {
        return segmentDelim;
    }

    public void setSegmentDelim(String segmentDelim) {
        this.segmentDelim = segmentDelim;
    }

    public String getGroupDelim() {
        return groupDelim;
    }

    public void setGroupDelim(String groupDelim) {
        this.groupDelim = groupDelim;
    }

    public String getFieldDelim() {
        return fieldDelim;
    }

    public void setFieldDelim(String fieldDelim) {
        this.fieldDelim = fieldDelim;
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
//		if (document != null && document.getElementsByTagName("ServiceProviderId") != null) {
//			Node sender = document.getElementsByTagName("ServiceProviderId").item(0);
//			if (sender != null) {
//				sendingFacility = sender.getTextContent();
//			}
//		}
		String event = "DICOM";
//		if (document != null && document.getElementsByTagName("TransactionCode") != null) {
//			Node type = document.getElementsByTagName("TransactionCode").item(0);
//			if (type != null) {
//				event = NCPDPReference.getInstance().getTransactionName(type.getTextContent());
//			}
//		}
		String version = "";
//		if (document != null && document.getElementsByTagName("VersionReleaseNumber") != null) {
//			Node versionNode = document.getElementsByTagName("VersionReleaseNumber").item(0);
//			if (versionNode != null) {
//				version = versionNode.getTextContent();
//			}
//		}
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
                        tagDesc = tagDesc.replaceAll(" ", "");
                        tagDesc = tagDesc.replaceAll("'", "");
                        try {
                            if(!tagDesc.equals("?"))
                                document.renameNode(node,null,tagDesc);
                        }
                        catch(DOMException e){
                            e.printStackTrace();
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

    private String convertToXML(DicomInputStream dis){
        StringWriter xmlOutput = new StringWriter();
        try{
            try {
                SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
                TransformerHandler th = tf.newTransformerHandler();
                th.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
                th.setResult(new StreamResult(xmlOutput));
                final SAXWriter writer = new SAXWriter(th, null);
                if(!includePixelData) {
                    writer.setExclude(new int[]{Tag.PixelData});
                }
                dis.setHandler(writer);
                dis.readDicomObject(new BasicDicomObject(), -1);
            }
            finally {
                dis.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return decodeTagNames(xmlOutput.toString());
    }
}
