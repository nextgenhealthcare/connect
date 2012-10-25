/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.ContentHandlerAdapter;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.SAXWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mirth.connect.server.util.DICOMUtil;

public class DICOMSerializer implements IXMLSerializer<String> {
    private String rawData;
    private List<String> pixelData;
    private DocumentSerializer documentSerializer = new DocumentSerializer();

    public DICOMSerializer() {

    }

    public DICOMSerializer(Map<?, ?> DICOMProperties) {

    }

    public static Map<String, String> getDefaultProperties() {
        return new HashMap<String, String>();
    }

    public List<String> getPixelData() {
        return pixelData;
    }

    public String getRawData() {
        return rawData;
    }

    @Override
    public String fromXML(String source) throws SerializerException {
        if (source == null || source.length() == 0) {
            return StringUtils.EMPTY;
        }

        try {
            // re-parse the xml to Mirth format
            Document document = documentSerializer.fromXML(source);
            Element element = document.getDocumentElement();
            Node node = element.getChildNodes().item(0);

            // change back to <attr> tag for all tags under <dicom> tag
            while (node != null) {
                renameTagToAttr(document, node);
                node = node.getNextSibling();
            }

            NodeList items = document.getElementsByTagName("item");

            // change back to <attr> tag for all tags under <item> tags
            if (items != null) {
                for (int i = 0; i < items.getLength(); i++) {
                    Node itemNode = items.item(i);

                    if (itemNode.getChildNodes() != null) {
                        NodeList itemNodes = itemNode.getChildNodes();

                        for (int j = 0; j < itemNodes.getLength(); j++) {
                            Node nodeItem = itemNodes.item(j);
                            renameTagToAttr(document, nodeItem);
                        }
                    }
                }
            }

            // find the charset
            String charset = null;
            Element charsetElement = (Element) document.getElementsByTagName("tag00080005").item(0);

            if (charsetElement != null) {
                charset = charsetElement.getNodeValue();
            } else {
                charset = "utf-8";
            }

            // parse the Document into a DicomObject
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            DicomObject dicomObject = new BasicDicomObject();
            ContentHandlerAdapter contentHandler = new ContentHandlerAdapter(dicomObject);
            byte[] documentBytes = documentSerializer.toXML(document).trim().getBytes(charset);
            parser.parse(new InputSource(new ByteArrayInputStream(documentBytes)), contentHandler);
            return new String(Base64.encodeBase64Chunked(DICOMUtil.dicomObjectToByteArray(dicomObject)));
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public String toXML(String source) throws SerializerException {
        try {
            DicomObject dicomObject = DICOMUtil.byteArrayToDicomObject(Base64.decodeBase64(source));
            // read in header and pixel data
            pixelData = extractPixelDataFromDicomObject(dicomObject);
            byte[] decodedMessage = DICOMUtil.dicomObjectToByteArray(dicomObject);
            rawData = new String(Base64.encodeBase64Chunked(decodedMessage));

            StringWriter output = new StringWriter();
            DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(decodedMessage));

            try {
                SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
                TransformerHandler handler = factory.newTransformerHandler();
                handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
                handler.setResult(new StreamResult(output));

                final SAXWriter writer = new SAXWriter(handler, null);
                dis.setHandler(writer);
                dis.readDicomObject(new BasicDicomObject(), -1);
                String serializedDicomObject = output.toString();
                
                // rename the "attr" element to the tag ID
                Document document = documentSerializer.fromXML(serializedDicomObject);
                NodeList attrElements = document.getElementsByTagName("attr");

                for (int i = 0; i < attrElements.getLength(); i++) {
                    Element attrElement = (Element) attrElements.item(i);
                    renameAttrToTag(document, attrElement);
                }
                
                return documentSerializer.toXML(document);
            } catch (Exception e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(dis);
                IOUtils.closeQuietly(output);

                if (dis != null) {
                    dis.close();
                }
            }
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    private Map<String, String> getMetadata(String sourceMessage) {
        return getMetadataFromDocument(documentSerializer.fromXML(sourceMessage));
    }

    @Override
    public Map<String, String> getMetadataFromDocument(Document document) {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("version", StringUtils.EMPTY);
        metadata.put("type", "DICOM");
        metadata.put("source", "dicom");
        return metadata;
    }

    @Override
    public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
        return getMetadata(fromXML(source));
    }

    @Override
    public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
        return getMetadata(xmlSource);
    }

    private void renameAttrToTag(Document document, Node node) throws DOMException {
        if (node.getNodeName().equals("attr")) {
            String tag = node.getAttributes().getNamedItem("tag").getNodeValue();

            if (!tag.equals("?")) {
                document.renameNode(node, null, "tag" + tag);
            }
        }
    }
    
    private void renameTagToAttr(Document document, Node node) throws DOMException {
        NamedNodeMap attr = node.getAttributes();

        if (attr != null) {
            Node tagAttr = attr.getNamedItem("tag");

            if (tagAttr != null) {
                String tag = "tag" + tagAttr.getNodeValue();

                if (!tag.equals("?") && tag.equals(node.getNodeName())) {
                    document.renameNode(node, null, "attr");
                }
            }
        }
    }

    /**
     * Revoves pixel data from the specified DicomObject and returns a list of
     * Base64 encoded image data.
     * 
     * @param dicomObject
     * @return
     */
    private List<String> extractPixelDataFromDicomObject(DicomObject dicomObject) {
        List<String> images = new ArrayList<String>();
        // this removes the data from the DICOM object
        DicomElement dicomElement = dicomObject.remove(Tag.PixelData);

        if (dicomElement != null) {
            if (dicomElement.hasItems()) {
                for (int i = 0; i < dicomElement.countItems(); i++) {
                    images.add(new String(Base64.encodeBase64Chunked(dicomElement.getFragment(i))));
                }
            } else {
                images.add(new String(Base64.encodeBase64Chunked(dicomElement.getBytes())));
            }
        }

        return images;
    }
}
