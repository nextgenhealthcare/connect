/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.dicom;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.dcm4che2.data.BasicDicomObject;
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

import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.model.converters.DICOMConverter;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DICOMSerializer implements IXMLSerializer {
    private DocumentSerializer documentSerializer = new DocumentSerializer();

    public DICOMSerializer() {

    }

    public DICOMSerializer(SerializerProperties DICOMProperties) {

    }

    public static Map<String, String> getDefaultProperties() {
        return new HashMap<String, String>();
    }

    public static byte[] removePixelData(byte[] content) throws IOException {
        DicomObject dicomObject = DICOMConverter.byteArrayToDicomObject(content, false);
        dicomObject.remove(Tag.PixelData);

        return DICOMConverter.dicomObjectToByteArray(dicomObject);
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) throws XmlSerializerException {
        return null;
    }

    @Override
    public String fromXML(String source) throws XmlSerializerException {
        if (source == null || source.length() == 0) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
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
            return StringUtils.newStringUsAscii(Base64Util.encodeBase64(DICOMConverter.dicomObjectToByteArray(dicomObject)));
        } catch (Exception e) {
            throw new XmlSerializerException("Error converting XML to DICOM", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting XML to DICOM", e));
        }
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        try {
            byte[] encodedMessage = org.apache.commons.codec.binary.StringUtils.getBytesUsAscii(source);

            StringWriter output = new StringWriter();
            DicomInputStream dis = new DicomInputStream(new BufferedInputStream(new Base64InputStream(new ByteArrayInputStream(encodedMessage))));
            /*
             * This parameter was added in dcm4che 2.0.28. We use it to retain the memory allocation
             * behavior from 2.0.25.
             * http://www.mirthcorp.com/community/issues/browse/MIRTH-2166
             * http://www.dcm4che.org/jira/browse/DCM-554
             */
            dis.setAllocateLimit(-1);

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
            throw new XmlSerializerException("Error converting DICOM to XML", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting DICOM to XML", e));
        }
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

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, "");
        map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, "DICOM");
        return map;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {}
}
