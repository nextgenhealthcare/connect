/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mirth.connect.donkey.util.DateParser;
import com.mirth.connect.donkey.util.DateParser.DateParserException;
import com.mirth.connect.donkey.util.migration.DonkeyElement;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.XmlUtil;

public class ImportConverter3_0_0 {
    private final static String VERSION_STRING = "3.0.0";
    private final static Pattern STRING_NODE_PATTERN = Pattern.compile("(?<=<(string)>).*(?=</string>)|<null/>");

    private static Logger logger = Logger.getLogger(ImportConverter3_0_0.class);

    /**
     * Takes a serialized object and using the expectedClass hint, runs the appropriate conversion
     * to convert the object to the 3.0.0 structure.
     * 
     * @param document
     *            A DOM document representation of the object
     * @param objectXml
     *            A serialized XML string representation of the object
     * @param expectedClass
     *            The expected class of the object (after migration to the LATEST version).
     * @return A DOM document representing the object in version 3.0.0 format
     */
    public static Document convert(Document document, String objectXml, Class<?> expectedClass) throws Exception {
        if (document.getDocumentElement().hasAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME)) {
            return document;
        }

        DocumentSerializer documentSerializer = new DocumentSerializer();
        logger.debug("Converting serialized object with expected class \"" + expectedClass.getName() + "\" to 3.0.0 structure");

        if (expectedClass == Channel.class) {
            document = documentSerializer.fromXML(ImportConverter.convertChannelString(objectXml));
            migrateChannel(new MirthElement(document.getDocumentElement()));
        } else if (expectedClass == Connector.class) {
            Element root = document.getDocumentElement();

            if (root.getNodeName().equals("list")) {
                NodeList listItems = root.getChildNodes();
                int itemCount = listItems.getLength();

                for (int i = (itemCount - 1); i >= 0; i--) {
                    if (listItems.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) listItems.item(i);

                        if (!element.hasAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME) && (element.getNodeName().equals("connector") || element.getNodeName().equals("com.mirth.connect.model.Connector") || element.getNodeName().equals("com.webreach.mirth.model.Connector"))) {
                            Element convertedConnector = XmlUtil.elementFromXml(ImportConverter.convertConnector(XmlUtil.elementToXml(element)));
                            migrateConnector(new MirthElement(convertedConnector), null);
                            root.replaceChild(document.importNode(convertedConnector, true), element);
                        }
                    }
                }
            } else {
                document = documentSerializer.fromXML(ImportConverter.convertConnector(objectXml));
                migrateConnector(new MirthElement(document.getDocumentElement()), null);
            }
        } else if (expectedClass == ChannelProperties.class) {
            DonkeyElement root = new MirthElement(document.getDocumentElement());
            migrateChannelProperties(root);
            root.setNodeName("channelProperties");
        } else if (expectedClass == CodeTemplate.class) {
            document = ImportConverter.convertCodeTemplates(objectXml);
            migrateCodeTemplate(new MirthElement(document.getDocumentElement()));
        } else if (expectedClass == ServerConfiguration.class) {
            document = ImportConverter.convertServerConfiguration(objectXml);
            migrateServerConfiguration(new MirthElement(document.getDocumentElement()));
        } else if (expectedClass == Filter.class) {
            document = documentSerializer.fromXML(ImportConverter.convertFilter(objectXml));
            // no 3.0.0 conversion is needed for the Filter class since it didn't change at all in 3.0.0
        }

        return document;
    }

    private static void migrateChannel(DonkeyElement channel) {
        logger.debug("Migrating channel to version " + VERSION_STRING);
        channel.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
//        channel.getChildElement("version").setTextContent(VERSION_STRING);
        channel.removeChild("version"); // TODO is it safe to remove the version property from the Channel class and other classes?

        // migrate channel properties
        Properties oldProperties = readPropertiesElement(channel.getChildElement("properties"));
        String synchronous = oldProperties.getProperty("synchronous", "true"); // use this later to set "waitForPrevious" on destination connectors
        migrateChannelProperties(channel.getChildElement("properties"));

        // migrate source connector
        DonkeyElement sourceConnector = channel.getChildElement("sourceConnector");
        migrateConnector(sourceConnector, 0);
        DonkeyElement responseConnectorProperties = sourceConnector.getChildElement("properties").getChildElement("responseConnectorProperties");

        // migrate destination connectors
        int metaDataId = 1;

        for (DonkeyElement destinationConnector : channel.getChildElement("destinationConnectors").getChildElements()) {
            migrateConnector(destinationConnector, metaDataId);
            destinationConnector.getChildElement("waitForPrevious").setTextContent(synchronous);

            // Fix response value
            if (responseConnectorProperties != null && destinationConnector.getChildElement("name").getTextContent().equals(responseConnectorProperties.getChildElement("responseVariable").getTextContent())) {
                responseConnectorProperties.getChildElement("responseVariable").setTextContent("d" + metaDataId);
            }

            metaDataId++;
        }

        channel.addChildElement("nextMetaDataId").setTextContent(Integer.toString(metaDataId));
    }

    private static void migrateConnector(DonkeyElement connector, Integer metaDataId) {
        logger.debug("Migrating connector");

        connector.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        DonkeyElement version = connector.getChildElement("version");

        if (version == null) {
            connector.addChildElement("version").setTextContent(VERSION_STRING);
        } else {
            version.setTextContent(VERSION_STRING);
        }

        // add metaDataId element
        String mode = connector.getChildElement("mode").getTextContent();

        if (metaDataId != null) {
            connector.addChildElement("metaDataId").setTextContent(metaDataId.toString());
        } else if (mode.equals("SOURCE")) {
            connector.addChildElement("metaDataId").setTextContent("0");
        }

        // convert transportName
        DonkeyElement transportName = connector.getChildElement("transportName");

        if (transportName.getTextContent().equals("JMS Reader")) {
            transportName.setTextContent("JMS Listener");
        } else if (transportName.getTextContent().equals("JMS Writer")) {
            transportName.setTextContent("JMS Sender");
        } else if (transportName.getTextContent().equals("LLP Listener")) {
            transportName.setTextContent("TCP Listener");
        } else if (transportName.getTextContent().equals("LLP Sender")) {
            transportName.setTextContent("TCP Sender");
        }

        // add a response transformer
        if (mode.equals("DESTINATION")) {
            createDefaultTransformer(connector.addChildElement("responseTransformer"));
        }

        // convert connector properties
        DonkeyElement properties = connector.getChildElement("properties");
        String dataType = readPropertiesElement(properties).getProperty("DataType");
        DonkeyElement transformer = connector.getChildElement("transformer");

        if (dataType.equals("Channel Reader")) {
            migrateVmReceiverProperties(properties);
        } else if (dataType.equals("Channel Writer")) {
            migrateVmDispatcherProperties(properties);
        } else if (dataType.equals("Database Reader")) {
            migrateDatabaseReceiverProperties(properties);
        } else if (dataType.equals("Database Writer")) {
            migrateDatabaseDispatcherProperties(properties);
        } else if (dataType.equals("DICOM Listener")) {
            migrateDICOMReceiverProperties(properties);
        } else if (dataType.equals("DICOM Sender")) {
            migrateDICOMDispatcherProperties(properties);
        } else if (dataType.equals("Document Writer")) {
            migrateDocumentDispatcherProperties(properties);
        } else if (dataType.equals("File Reader")) {
            migrateFileReceiverProperties(properties);
        } else if (dataType.equals("File Writer")) {
            migrateFileDispatcherProperties(properties);
        } else if (dataType.equals("HTTP Listener")) {
            migrateHttpReceiverProperties(properties);
        } else if (dataType.equals("HTTP Sender")) {
            migrateHttpDispatcherProperties(properties);
        } else if (dataType.equals("JavaScript Reader")) {
            migrateJavaScriptReceiverProperties(properties);
        } else if (dataType.equals("JavaScript Writer")) {
            migrateJavaScriptDispatcherProperties(properties);
        } else if (dataType.equals("JMS Reader")) {
            migrateJmsReceiverProperties(properties);
        } else if (dataType.equals("JMS Writer")) {
            migrateJmsDispatcherProperties(properties);
        } else if (dataType.equals("LLP Listener")) {
            // Some properties have been moved from the LLP Listener connector to the HL7 v2.x data type
            if (transformer.getChildElement("inboundProtocol").getTextContent().equals("HL7V2")) {
                Properties connectorProperties = readPropertiesElement(properties);
                DonkeyElement inboundProperties = transformer.getChildElement("inboundProperties");
                if (inboundProperties == null) {
                    inboundProperties = transformer.addChildElement("inboundProperties");
                }

                boolean frameEncodingHex = connectorProperties.getProperty("charEncoding", "hex").equals("hex");
                addChildAndSetName(inboundProperties, "segmentDelimiter").setTextContent(convertToEscapedString(connectorProperties.getProperty("segmentEnd", "0D"), frameEncodingHex));
                addChildAndSetName(inboundProperties, "successfulACKCode").setTextContent(connectorProperties.getProperty("ackCodeSuccessful", "AA"));
                addChildAndSetName(inboundProperties, "successfulACKMessage").setTextContent(connectorProperties.getProperty("ackMsgSuccessful", ""));
                addChildAndSetName(inboundProperties, "errorACKCode").setTextContent(connectorProperties.getProperty("ackCodeError", "AE"));
                addChildAndSetName(inboundProperties, "errorACKMessage").setTextContent(connectorProperties.getProperty("ackMsgError", "An Error Occured Processing Message."));
                addChildAndSetName(inboundProperties, "rejectedACKCode").setTextContent(connectorProperties.getProperty("ackCodeRejected", "AR"));
                addChildAndSetName(inboundProperties, "rejectedACKMessage").setTextContent(connectorProperties.getProperty("ackMsgRejected", "Message Rejected."));
                addChildAndSetName(inboundProperties, "msh15ACKAccept").setTextContent(readBooleanProperty(connectorProperties, "checkMSH15", false));
            }

            migrateLLPListenerProperties(properties);
        } else if (dataType.equals("LLP Sender")) {
            migrateLLPSenderProperties(properties);
        } else if (dataType.equals("TCP Listener")) {
            migrateTCPListenerProperties(properties);
        } else if (dataType.equals("TCP Sender")) {
            migrateTCPSenderProperties(properties);
        } else if (dataType.equals("SMTP Sender")) {
            migrateSmtpDispatcherProperties(properties);
        } else if (dataType.equals("Web Service Listener")) {
            migrateWebServiceListenerProperties(properties);
        } else if (dataType.equals("Web Service Sender")) {
            migrateWebServiceSenderProperties(properties);
        }

        // convert transformer (no conversion needed for filter since it didn't change at all in 3.0.0)
        migrateTransformer(transformer);

        // default waitForPrevious to true
        connector.addChildElement("waitForPrevious").setTextContent("true");
    }

    /*
     * This is used for properties that are set on the not-yet-changed element. The
     * readPropertiesElement method checks the "name" attribute, so we need to set it here
     * beforehand.
     */
    private static DonkeyElement addChildAndSetName(DonkeyElement parent, String name) {
        DonkeyElement child = parent.addChildElement(name);
        child.setAttribute("name", name);
        return child;
    }

    private static void migrateChannelProperties(DonkeyElement properties) {
        logger.debug("Migrating channel properties");

        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        Properties oldProperties = readPropertiesElement(properties);
        properties.removeChildren();

        properties.addChildElement("clearGlobalChannelMap").setTextContent(oldProperties.getProperty("clearGlobalChannelMap", "true"));

        if (oldProperties.getProperty("store_messages", "true").equals("true")) {
            properties.addChildElement("messageStorageMode").setTextContent("DEVELOPMENT");
        } else {
            properties.addChildElement("messageStorageMode").setTextContent("DISABLED");
        }

        properties.addChildElement("encryptData").setTextContent(oldProperties.getProperty("encryptData", "false"));
        properties.addChildElement("removeContentOnCompletion").setTextContent("false");
        properties.addChildElement("removeAttachmentsOnCompletion").setTextContent("false");
        properties.addChildElement("initialStateStarted").setTextContent((oldProperties.getProperty("initialState", "started").equals("started") ? "true" : "false"));
        properties.addChildElement("storeAttachments").setTextContent("false");
        properties.addChildElement("tags").setAttribute("class", "linked-hash-set");
        properties.addChildElement("metaDataColumns");
        properties.addChildElement("archiveEnabled").setTextContent("true");

        DonkeyElement attachmentProperties = properties.addChildElement("attachmentProperties");
        attachmentProperties.addChildElement("type").setTextContent("None");
        attachmentProperties.addChildElement("properties");

        String maxMessageAge = oldProperties.getProperty("max_message_age");

        if (!StringUtils.isBlank(maxMessageAge) && !maxMessageAge.equals("-1")) {
            properties.addChildElement("pruneMetaDataDays").setTextContent(maxMessageAge);
        }
    }

    private static void migrateCodeTemplate(DonkeyElement codeTemplate) {
        codeTemplate.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        codeTemplate.removeChild("version");
    }

    private static void migrateAlert(DonkeyElement alert) {
        // TODO
    }

    private static void migrateServerConfiguration(DonkeyElement serverConfiguration) {
        serverConfiguration.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        DonkeyElement channels = serverConfiguration.getChildElement("channels");

        if (channels != null) {
            for (DonkeyElement channel : channels.getChildElements()) {
                migrateChannel(channel);
            }
        }

        DonkeyElement alerts = serverConfiguration.getChildElement("alerts");

        if (alerts != null) {
            for (DonkeyElement alert : alerts.getChildElements()) {
                migrateAlert(alert);
            }
        }

        DonkeyElement codeTemplates = serverConfiguration.getChildElement("codeTemplates");

        if (codeTemplates != null) {
            for (DonkeyElement codeTemplate : codeTemplates.getChildElements()) {
                migrateCodeTemplate(codeTemplate);
            }
        }

        DonkeyElement pluginProperties = serverConfiguration.getChildElement("pluginProperties");

        if (pluginProperties != null) {
            for (DonkeyElement entry : pluginProperties.getChildElements()) {
                DonkeyElement pluginName = entry.getChildElement("string");

                if (pluginName.getTextContent().equals("Message Pruner")) {
                    pluginName.setTextContent("Data Pruner");
                    convertDataPrunerProperties(entry.getChildElement("properties"));
                }
            }
        }
    }

    private static void convertDataPrunerProperties(DonkeyElement propertiesElement) {
        Properties properties = readPropertiesElement(propertiesElement);

        properties.remove("allowBatchPruning");
        properties.setProperty("archiveEnabled", "false");

        writePropertiesElement(propertiesElement, properties);
    }

    private static void migrateTransformer(DonkeyElement transformer) {
        logger.debug("Migrating Transformer");
        transformer.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        // TODO make sure that protocol/data type names haven't changed in 3.0.0
        DonkeyElement inboundDataType = transformer.getChildElement("inboundProtocol");
        DonkeyElement outboundDataType = transformer.getChildElement("outboundProtocol");
        inboundDataType.setNodeName("inboundDataType");
        outboundDataType.setNodeName("outboundDataType");

        DonkeyElement inboundProperties = transformer.getChildElement("inboundProperties");
        DonkeyElement outboundProperties = transformer.getChildElement("outboundProperties");

        /*
         * We set the inbound and outbound HL7v2 segment delimiters here because each property set
         * is dependent on the other. If at least one of them has "Convert LF to CR" enabled, then
         * it can affect the value of the segment delimiters set on both elements.
         */
        if (inboundDataType.getTextContent().equals("HL7V2") || outboundDataType.getTextContent().equals("HL7V2")) {
            boolean convertLFtoCRInbound = readBooleanValue(readPropertiesElement(inboundProperties), "convertLFtoCR", false);
            boolean convertLFtoCROutbound = readBooleanValue(readPropertiesElement(outboundProperties), "convertLFtoCR", false);

            if (inboundDataType.getTextContent().equals("HL7V2") && inboundProperties != null) {
                setHL7v2SegmentDelimiters(inboundProperties, convertLFtoCRInbound, convertLFtoCROutbound);
            }
            if (outboundDataType.getTextContent().equals("HL7V2") && outboundProperties != null) {
                setHL7v2SegmentDelimiters(outboundProperties, convertLFtoCRInbound, convertLFtoCROutbound);
            }
        }

        if (inboundProperties != null) {
            migrateDataTypeProperties(inboundProperties, inboundDataType.getTextContent());
        }

        if (outboundProperties != null) {
            migrateDataTypeProperties(outboundProperties, outboundDataType.getTextContent());
        }

        // Rename EDI and X12 data types to "EDI/X12"
        if (inboundDataType.getTextContent().equals("EDI") || inboundDataType.getTextContent().equals("X12")) {
            inboundDataType.setTextContent("EDI/X12");
        }
        if (outboundDataType.getTextContent().equals("EDI") || outboundDataType.getTextContent().equals("X12")) {
            outboundDataType.setTextContent("EDI/X12");
        }
    }

    private static void setHL7v2SegmentDelimiters(DonkeyElement properties, boolean convertLFtoCRInbound, boolean convertLFtoCROutbound) {
        String segmentDelimiter;
        String inboundSegmentDelimiter;
        String outboundSegmentDelimiter;

        // If we've manually set a segment delimiter (i.e. with an LLP Listener), retrieve that here
        if (properties.getChildElement("segmentDelimiter") != null) {
            segmentDelimiter = properties.getChildElement("segmentDelimiter").getTextContent();
        } else {
            segmentDelimiter = "\\r";
        }

        if (!convertLFtoCRInbound && !convertLFtoCROutbound) {
            /*
             * If "Convert LF to CR" is disabled on both the inbound and outbound side, then no
             * replacement will be done. Therefore, the expected inbound and outbound delimiters
             * will be the same.
             */
            inboundSegmentDelimiter = outboundSegmentDelimiter = segmentDelimiter;
        } else {
            /*
             * If "Convert LF to CR" is enabled on at least one side, then there may be more than
             * one accepted inbound delimiter, and the outbound delimiter will always be a carriage
             * return.
             */
            if (segmentDelimiter.equals("\\r")) {
                inboundSegmentDelimiter = "\\r\\n|\\r|\\n";
            } else if (segmentDelimiter.equals("\\n")) {
                inboundSegmentDelimiter = "\\r\\n|\\n";
            } else {
                inboundSegmentDelimiter = "\\r\\n|\\n|" + segmentDelimiter;
            }

            outboundSegmentDelimiter = "\\r";
        }

        addChildAndSetName(properties, "inboundSegmentDelimiter").setTextContent(inboundSegmentDelimiter);
        addChildAndSetName(properties, "outboundSegmentDelimiter").setTextContent(outboundSegmentDelimiter);
    }

    private static void migrateDataTypeProperties(DonkeyElement properties, String dataType) {
        if (dataType.equals("DELIMITED")) {
            migrateDelimitedProperties(properties);
        } else if (dataType.equals("DICOM")) {
            migrateDICOMProperties(properties);
        } else if (dataType.equals("EDI")) {
            migrateEDIProperties(properties);
        } else if (dataType.equals("HL7V2")) {
            migrateHL7v2Properties(properties);
        } else if (dataType.equals("HL7V3")) {
            migrateHL7v3Properties(properties);
        } else if (dataType.equals("NCPDP")) {
            migrateNCPDPProperties(properties);
        } else if (dataType.equals("X12")) {
            migrateX12Properties(properties);
        } else if (dataType.equals("XML")) {
            migrateXMLProperties(properties);
        }
    }

    private static void migrateVmReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating VmReceiverProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.vm.VmReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), true);
    }

    private static void migrateVmDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating VmDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.vm.VmDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        String host = oldProperties.getProperty("host", "none");

        if (host.equals("sink")) {
            host = "none";
        }

        properties.addChildElement("channelId").setTextContent(host);
        properties.addChildElement("channelTemplate").setTextContent(oldProperties.getProperty("template", "${message.encodedData}"));
    }

    private static void migrateDICOMReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating DICOMReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.dimse.DICOMReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "104"));

        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), false);
        properties.addChildElement("soCloseDelay").setTextContent(oldProperties.getProperty("soclosedelay", "50"));
        properties.addChildElement("releaseTo").setTextContent(oldProperties.getProperty("releaseto", "5"));
        properties.addChildElement("requestTo").setTextContent(oldProperties.getProperty("requestto", "5"));
        properties.addChildElement("idleTo").setTextContent(oldProperties.getProperty("idleto", "60"));
        properties.addChildElement("reaper").setTextContent(oldProperties.getProperty("reaper", "10"));
        properties.addChildElement("rspDelay").setTextContent(oldProperties.getProperty("rspdelay", "0"));
        properties.addChildElement("pdv1").setTextContent(readBooleanProperty(oldProperties, "pdv1", false));
        properties.addChildElement("sndpdulen").setTextContent(oldProperties.getProperty("sndpdulen", "16"));
        properties.addChildElement("rcvpdulen").setTextContent(oldProperties.getProperty("rcvpdulen", "16"));
        properties.addChildElement("async").setTextContent(oldProperties.getProperty("async", "0"));
        properties.addChildElement("bigEndian").setTextContent(readBooleanProperty(oldProperties, "bigendian", false));
        properties.addChildElement("bufSize").setTextContent(oldProperties.getProperty("bufsize", "1"));
        properties.addChildElement("defts").setTextContent(readBooleanProperty(oldProperties, "defts", false));
        properties.addChildElement("dest").setTextContent(oldProperties.getProperty("dest", ""));
        properties.addChildElement("nativeData").setTextContent(readBooleanProperty(oldProperties, "nativeData", false));
        properties.addChildElement("sorcvbuf").setTextContent(oldProperties.getProperty("sorcvbuf", "0"));
        properties.addChildElement("sosndbuf").setTextContent(oldProperties.getProperty("sosndbuf", "0"));
        properties.addChildElement("tcpDelay").setTextContent(readBooleanProperty(oldProperties, "tcpdelay", true));
        properties.addChildElement("keyPW").setTextContent(oldProperties.getProperty("keypw", ""));
        properties.addChildElement("keyStore").setTextContent(oldProperties.getProperty("keystore", ""));
        properties.addChildElement("keyStorePW").setTextContent(oldProperties.getProperty("keystorepw", ""));
        properties.addChildElement("noClientAuth").setTextContent(readBooleanProperty(oldProperties, "noclientauth", true));
        properties.addChildElement("nossl2").setTextContent(readBooleanProperty(oldProperties, "nossl2", true));
        properties.addChildElement("tls").setTextContent(oldProperties.getProperty("tls", "notls"));
        properties.addChildElement("trustStore").setTextContent(oldProperties.getProperty("truststore", ""));
        properties.addChildElement("trustStorePW").setTextContent(oldProperties.getProperty("truststorepw", ""));
        properties.addChildElement("applicationEntity").setTextContent(oldProperties.getProperty("applicationEntity", ""));
        properties.addChildElement("localHost").setTextContent(oldProperties.getProperty("localHost", ""));
        properties.addChildElement("localPort").setTextContent(oldProperties.getProperty("localPort", ""));
        properties.addChildElement("localApplicationEntity").setTextContent(oldProperties.getProperty("localApplicationEntity", ""));
    }

    private static void migrateDICOMDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating DICOMDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.dimse.DICOMDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "127.0.0.1"));
        properties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "104"));
        properties.addChildElement("applicationEntity").setTextContent(oldProperties.getProperty("applicationEntity", ""));
        properties.addChildElement("localHost").setTextContent(oldProperties.getProperty("localHost", ""));
        properties.addChildElement("localPort").setTextContent(oldProperties.getProperty("localPort", ""));
        properties.addChildElement("localApplicationEntity").setTextContent(oldProperties.getProperty("localApplicationEntity", ""));
        properties.addChildElement("template").setTextContent(oldProperties.getProperty("template", "${DICOMMESSAGE}"));
        properties.addChildElement("acceptTo").setTextContent(oldProperties.getProperty("acceptto", "5000"));
        properties.addChildElement("async").setTextContent(oldProperties.getProperty("async", "0"));
        properties.addChildElement("bufSize").setTextContent(oldProperties.getProperty("bufsize", "1"));
        properties.addChildElement("connectTo").setTextContent(oldProperties.getProperty("connectto", "0"));
        properties.addChildElement("priority").setTextContent(oldProperties.getProperty("priority", "med"));
        properties.addChildElement("passcode").setTextContent(oldProperties.getProperty("passcode", ""));
        properties.addChildElement("pdv1").setTextContent(readBooleanProperty(oldProperties, "pdv1", false));
        properties.addChildElement("rcvpdulen").setTextContent(oldProperties.getProperty("rcvpdulen", "16"));
        properties.addChildElement("reaper").setTextContent(oldProperties.getProperty("reaper", "10"));
        properties.addChildElement("releaseTo").setTextContent(oldProperties.getProperty("releaseto", "5"));
        properties.addChildElement("rspTo").setTextContent(oldProperties.getProperty("rspto", "60"));
        properties.addChildElement("shutdownDelay").setTextContent(oldProperties.getProperty("shutdowndelay", "1000"));
        properties.addChildElement("sndpdulen").setTextContent(oldProperties.getProperty("sndpdulen", "16"));
        properties.addChildElement("soCloseDelay").setTextContent(oldProperties.getProperty("soclosedelay", "50"));
        properties.addChildElement("sorcvbuf").setTextContent(oldProperties.getProperty("sorcvbuf", "0"));
        properties.addChildElement("sosndbuf").setTextContent(oldProperties.getProperty("sosndbuf", "0"));
        properties.addChildElement("stgcmt").setTextContent(readBooleanProperty(oldProperties, "stgcmt", false));
        properties.addChildElement("tcpDelay").setTextContent(readBooleanProperty(oldProperties, "tcpdelay", true));
        properties.addChildElement("ts1").setTextContent(readBooleanProperty(oldProperties, "pdv1", false));
        properties.addChildElement("uidnegrsp").setTextContent(readBooleanProperty(oldProperties, "uidnegrsp", false));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("username", ""));
        properties.addChildElement("keyPW").setTextContent(oldProperties.getProperty("keypw", ""));
        properties.addChildElement("keyStore").setTextContent(oldProperties.getProperty("keystore", ""));
        properties.addChildElement("keyStorePW").setTextContent(oldProperties.getProperty("keystorepw", ""));
        properties.addChildElement("noClientAuth").setTextContent(readBooleanProperty(oldProperties, "noclientauth", true));
        properties.addChildElement("nossl2").setTextContent(readBooleanProperty(oldProperties, "nossl2", true));
        properties.addChildElement("tls").setTextContent(oldProperties.getProperty("tls", "notls"));
        properties.addChildElement("trustStore").setTextContent(oldProperties.getProperty("truststore", ""));
        properties.addChildElement("trustStorePW").setTextContent(oldProperties.getProperty("truststorepw", ""));
    }

    private static void migrateDocumentDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating DocumentDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.doc.DocumentDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));
        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", ""));
        properties.addChildElement("outputPattern").setTextContent(oldProperties.getProperty("outputPattern", ""));
        properties.addChildElement("documentType").setTextContent(oldProperties.getProperty("documentType", "pdf"));
        properties.addChildElement("encrypt").setTextContent(readBooleanProperty(oldProperties, "encrypt", false));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("password", ""));
        properties.addChildElement("template").setTextContent(oldProperties.getProperty("template", ""));
    }

    private static void migrateFileReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating FileReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.file.FileReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        migratePollConnectorProperties(properties.addChildElement("pollConnectorProperties"), oldProperties.getProperty("pollingType"), oldProperties.getProperty("pollingTime"), oldProperties.getProperty("pollingFrequency"));
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), "None", true);

        properties.addChildElement("scheme").setTextContent(oldProperties.getProperty("scheme", "file"));
        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", ""));
        properties.addChildElement("fileFilter").setTextContent(oldProperties.getProperty("fileFilter", "*"));
        properties.addChildElement("regex").setTextContent(readBooleanProperty(oldProperties, "regex", false));
        properties.addChildElement("ignoreDot").setTextContent(readBooleanProperty(oldProperties, "ignoreDot", true));
        properties.addChildElement("anonymous").setTextContent(readBooleanProperty(oldProperties, "FTPAnonymous", true));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("username", "anonymous"));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("password", "anonymous"));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("timeout", "10000"));
        properties.addChildElement("secure").setTextContent(readBooleanProperty(oldProperties, "secure", true));
        properties.addChildElement("passive").setTextContent(readBooleanProperty(oldProperties, "passive", true));
        properties.addChildElement("validateConnection").setTextContent(readBooleanProperty(oldProperties, "validateConnections", true));
        properties.addChildElement("checkFileAge").setTextContent(readBooleanProperty(oldProperties, "checkFileAge", true));
        properties.addChildElement("fileAge").setTextContent(oldProperties.getProperty("fileAge", "1000"));
        properties.addChildElement("sortBy").setTextContent(oldProperties.getProperty("sortAttribute", "date"));
        properties.addChildElement("binary").setTextContent(readBooleanProperty(oldProperties, "binary", false));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("processBatch").setTextContent(readBooleanProperty(oldProperties, "processBatchFiles", false));

        String moveToDirectory = oldProperties.getProperty("moveToDirectory");
        String moveToFileName = oldProperties.getProperty("moveToPattern");

        properties.addChildElement("moveToDirectory").setTextContent(moveToDirectory);
        properties.addChildElement("moveToFileName").setTextContent(moveToFileName);

        String afterProcessingAction = "None";

        if (Boolean.parseBoolean(oldProperties.getProperty("autoDelete", "false"))) {
            afterProcessingAction = "Delete";
        } else if (!StringUtils.isBlank(moveToDirectory) || !StringUtils.isBlank(moveToFileName)) {
            afterProcessingAction = "Move";
        }

        properties.addChildElement("afterProcessingAction").setTextContent(afterProcessingAction);

        String errorMoveToDirectory = oldProperties.getProperty("moveToErrorDirectory");
        String errorReadingAction = "None";

        if (!StringUtils.isBlank(errorMoveToDirectory)) {
            errorReadingAction = "Move";
        }

        properties.addChildElement("errorReadingAction").setTextContent(errorReadingAction);
        properties.addChildElement("errorMoveToDirectory").setTextContent(errorMoveToDirectory);
        properties.addChildElement("errorMoveToFileName").setTextContent("");
        properties.addChildElement("errorResponseAction").setTextContent("After Processing Action");
    }

    private static void migrateFileDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating FileDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.file.FileDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("scheme").setTextContent(oldProperties.getProperty("scheme", "file"));
        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", ""));
        properties.addChildElement("outputPattern").setTextContent(oldProperties.getProperty("outputPattern", ""));
        properties.addChildElement("anonymous").setTextContent(readBooleanProperty(oldProperties, "FTPAnonymous", true));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("username", "anonymous"));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("password", "anonymous"));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("timeout", "10000"));
        properties.addChildElement("secure").setTextContent(readBooleanProperty(oldProperties, "secure", true));
        properties.addChildElement("passive").setTextContent(readBooleanProperty(oldProperties, "passive", true));
        properties.addChildElement("validateConnection").setTextContent(readBooleanProperty(oldProperties, "validateConnections", true));
        properties.addChildElement("outputAppend").setTextContent(readBooleanProperty(oldProperties, "outputAppend", true));
        properties.addChildElement("errorOnExists").setTextContent(readBooleanProperty(oldProperties, "errorOnExists", false));
        properties.addChildElement("temporary").setTextContent(readBooleanProperty(oldProperties, "temporary", false));
        properties.addChildElement("binary").setTextContent(readBooleanProperty(oldProperties, "binary", false));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("template").setTextContent(oldProperties.getProperty("template", ""));
    }

    private static void migrateHttpReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating HttpReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.http.HttpReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildListenerConnectorProperties(properties.addChildElement("listenerConnectorProperties"), oldProperties.getProperty("host"), oldProperties.getProperty("port"), 80);
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("receiverResponse"), true);

        properties.addChildElement("bodyOnly").setTextContent(readBooleanProperty(oldProperties, "receiverBodyOnly", true));
        properties.addChildElement("responseContentType").setTextContent(oldProperties.getProperty("receiverResponseContentType", "text/plain"));
        properties.addChildElement("responseStatusCode").setTextContent(oldProperties.getProperty("receiverResponseStatusCode", ""));
        properties.addChildElement("responseHeaders").setTextContent(oldProperties.getProperty("receiverResponseHeaders", "&lt;linked-hash-map/&gt;"));
        properties.addChildElement("charset").setTextContent(oldProperties.getProperty("receiverCharset", "UTF-8"));
        properties.addChildElement("contextPath").setTextContent(oldProperties.getProperty("receiverContextPath", ""));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("receiverTimeout", "0"));
    }

    private static void migrateHttpDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating HttpDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.http.HttpDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), null);

        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", ""));
        properties.addChildElement("method").setTextContent(oldProperties.getProperty("dispatcherMethod", "post"));
        properties.addChildElement("headers").setTextContent(oldProperties.getProperty("dispatcherHeaders", "&lt;linked-hash-map/&gt;"));
        properties.addChildElement("parameters").setTextContent(oldProperties.getProperty("dispatcherParameters", "&lt;linked-hash-map/&gt;"));
        properties.addChildElement("includeHeadersInResponse").setTextContent(readBooleanProperty(oldProperties, "dispatcherIncludeHeadersInResponse", false));
        properties.addChildElement("multipart").setTextContent(readBooleanProperty(oldProperties, "dispatcherMultipart", false));
        properties.addChildElement("useAuthentication").setTextContent(readBooleanProperty(oldProperties, "dispatcherUseAuthentication", false));
        properties.addChildElement("authenticationType").setTextContent(oldProperties.getProperty("dispatcherAuthenticationType", "Basic"));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("dispatcherUsername", ""));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("dispatcherPassword", ""));
        properties.addChildElement("content").setTextContent(oldProperties.getProperty("dispatcherContent", ""));
        properties.addChildElement("contentType").setTextContent(oldProperties.getProperty("dispatcherContentType", "text/plain"));
        properties.addChildElement("charset").setTextContent(oldProperties.getProperty("dispatcherCharset", "UTF-8"));
        properties.addChildElement("socketTimeout").setTextContent(oldProperties.getProperty("dispatcherSocketTimeout", "30000"));
    }

    private static void migrateDatabaseReceiverProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateDatabaseDispatcherProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateJmsReceiverProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateJmsDispatcherProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateJavaScriptReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating JavaScriptReceiverProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.js.JavaScriptReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        migratePollConnectorProperties(properties.addChildElement("pollConnectorProperties"), oldProperties.getProperty("pollingType"), oldProperties.getProperty("pollingTime"), oldProperties.getProperty("pollingFrequency"));
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), false);

        properties.addChildElement("script").setTextContent(oldProperties.getProperty("script", ""));
    }

    private static void migrateJavaScriptDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating JavaScriptDispatcherProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.js.JavaScriptDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("script").setTextContent(oldProperties.getProperty("script", ""));
    }

    private static void migrateSmtpDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating SmtpDispatcherProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.smtp.SmtpDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("attachments").setTextContent(oldProperties.getProperty("attachments", "&lt;list/&gt;"));
        properties.addChildElement("authentication").setTextContent(readBooleanProperty(oldProperties, "authentication", false));
        properties.addChildElement("body").setTextContent(oldProperties.getProperty("body", ""));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("encryption").setTextContent(oldProperties.getProperty("encryption", "none"));
        properties.addChildElement("from").setTextContent(oldProperties.getProperty("from", ""));
        properties.addChildElement("headers").setTextContent(oldProperties.getProperty("headers", "&lt;linked-hash-map/&gt;"));
        properties.addChildElement("html").setTextContent(readBooleanProperty(oldProperties, "html", false));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("password", ""));
        properties.addChildElement("smtpHost").setTextContent(oldProperties.getProperty("smtpHost", ""));
        properties.addChildElement("smtpPort").setTextContent(oldProperties.getProperty("smtpPort", "25"));
        properties.addChildElement("subject").setTextContent(oldProperties.getProperty("subject", "25"));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("timeout", "5000"));
        properties.addChildElement("to").setTextContent(oldProperties.getProperty("to", ""));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("username", ""));

        properties.addChildElement("cc").setTextContent("");
        properties.addChildElement("bcc").setTextContent("");
        properties.addChildElement("replyTo").setTextContent("");
    }

    private static void migrateLLPListenerProperties(DonkeyElement properties) {
        logger.debug("Migrating LLPListenerProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "6661"));

        String responseValue = oldProperties.getProperty("responseValue", "None");
        if (readBooleanValue(oldProperties, "sendACK", true)) {
            responseValue = "Auto-generate (After source transformer)";
        }
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), responseValue, true);

        DonkeyElement transmissionModeProperties = properties.addChildElement("transmissionModeProperties");
        transmissionModeProperties.setAttribute("class", "com.mirth.connect.model.transmission.framemode.FrameModeProperties");
        transmissionModeProperties.addChildElement("pluginPointName").setTextContent("MLLP");

        boolean frameEncodingHex = oldProperties.getProperty("charEncoding", "hex").equals("hex");
        String startOfMessageBytes;
        String endOfMessageBytes;
        if (frameEncodingHex) {
            startOfMessageBytes = stripHexPrefix(oldProperties.getProperty("messageStart", "0B"));
            endOfMessageBytes = stripHexPrefix(oldProperties.getProperty("messageEnd", "1C")) + stripHexPrefix(oldProperties.getProperty("recordSeparator", "0D"));
        } else {
            startOfMessageBytes = convertToHexString(oldProperties.getProperty("messageStart", "\u000B"));
            endOfMessageBytes = convertToHexString(oldProperties.getProperty("messageEnd", "\u001C") + oldProperties.getProperty("recordSeparator", "\r"));
        }
        transmissionModeProperties.addChildElement("startOfMessageBytes").setTextContent(startOfMessageBytes);
        transmissionModeProperties.addChildElement("endOfMessageBytes").setTextContent(endOfMessageBytes);

        properties.addChildElement("serverMode").setTextContent(readBooleanProperty(oldProperties, "serverMode", true));
        properties.addChildElement("reconnectInterval").setTextContent(oldProperties.getProperty("reconnectInterval", "5000"));
        properties.addChildElement("receiveTimeout").setTextContent(oldProperties.getProperty("receiveTimeout", "0"));
        properties.addChildElement("bufferSize").setTextContent(oldProperties.getProperty("bufferSize", "65536"));
        properties.addChildElement("maxConnections").setTextContent("10");
        properties.addChildElement("keepConnectionOpen").setTextContent("true");
        properties.addChildElement("processBatch").setTextContent(readBooleanProperty(oldProperties, "processBatchFiles", false));
        properties.addChildElement("dataTypeBinary").setTextContent("false");
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("respondOnNewConnection").setTextContent(readBooleanValue(oldProperties, "ackOnNewConnection", false) ? "1" : "0");
        properties.addChildElement("responseAddress").setTextContent(oldProperties.getProperty("ackIP", ""));
        properties.addChildElement("responsePort").setTextContent(oldProperties.getProperty("ackPort", ""));
    }

    /*
     * Strips off "0x" or "0X" from the beginning of a string.
     */
    private static String stripHexPrefix(String hexString) {
        return hexString.startsWith("0x") || hexString.startsWith("0X") ? hexString.substring(2) : hexString;
    }

    /*
     * Converts a string (either in hexadecimal format or in literal ASCII) to a Java-escaped
     * format. Any instances of character 0x0D gets converted to "\\r", 0x0B to "\\u000B", etc.
     */
    private static String convertToEscapedString(String str, boolean hex) {
        String fixedString = str;
        if (hex) {
            byte[] bytes = stringToByteArray(stripHexPrefix(str));
            try {
                fixedString = new String(bytes, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                fixedString = new String(bytes);
            }
        }
        return StringEscapeUtils.escapeJava(fixedString);
    }

    /*
     * Converts an ASCII string into a byte array, and then returns the associated hexadecimal
     * representation of the bytes.
     */
    private static String convertToHexString(String str) {
        try {
            return Hex.encodeHexString(str.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            return Hex.encodeHexString(str.getBytes());
        }
    }

    /*
     * Converts a hexadecimal string into a byte array, where each pair of characters is represented
     * as a single byte.
     */
    private static byte[] stringToByteArray(String str) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if (StringUtils.isNotBlank(str)) {
            String hexString = str.toUpperCase().replaceAll("[^0-9A-F]", "");

            for (String hexByte : ((hexString.length() % 2 > 0 ? "0" : "") + hexString).split("(?<=\\G..)")) {
                bytes.write((byte) ((Character.digit(hexByte.charAt(0), 16) << 4) + Character.digit(hexByte.charAt(1), 16)));
            }
        }

        return bytes.toByteArray();
    }

    private static void migrateLLPSenderProperties(DonkeyElement properties) {
        logger.debug("Migrating LLPSenderProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), oldProperties.getProperty("maxRetryCount"));

        DonkeyElement transmissionModeProperties = properties.addChildElement("transmissionModeProperties");
        transmissionModeProperties.setAttribute("class", "com.mirth.connect.model.transmission.framemode.FrameModeProperties");
        transmissionModeProperties.addChildElement("pluginPointName").setTextContent("MLLP");

        boolean frameEncodingHex = oldProperties.getProperty("charEncoding", "hex").equals("hex");
        String startOfMessageBytes;
        String endOfMessageBytes;
        if (frameEncodingHex) {
            startOfMessageBytes = stripHexPrefix(oldProperties.getProperty("messageStart", "0B"));
            endOfMessageBytes = stripHexPrefix(oldProperties.getProperty("messageEnd", "1C")) + stripHexPrefix(oldProperties.getProperty("recordSeparator", "0D"));
        } else {
            startOfMessageBytes = convertToHexString(oldProperties.getProperty("messageStart", "\u000B"));
            endOfMessageBytes = convertToHexString(oldProperties.getProperty("messageEnd", "\u001C") + oldProperties.getProperty("recordSeparator", "\r"));
        }
        transmissionModeProperties.addChildElement("startOfMessageBytes").setTextContent(startOfMessageBytes);
        transmissionModeProperties.addChildElement("endOfMessageBytes").setTextContent(endOfMessageBytes);

        properties.addChildElement("remoteAddress").setTextContent(oldProperties.getProperty("host", "127.0.0.1"));
        properties.addChildElement("remotePort").setTextContent(oldProperties.getProperty("port", "6660"));
        properties.addChildElement("overrideLocalBinding").setTextContent("false");
        properties.addChildElement("localAddress").setTextContent("0.0.0.0");
        properties.addChildElement("localPort").setTextContent("0");
        properties.addChildElement("sendTimeout").setTextContent(oldProperties.getProperty("sendTimeout", "5000"));
        properties.addChildElement("bufferSize").setTextContent(oldProperties.getProperty("bufferSize", "65536"));
        properties.addChildElement("keepConnectionOpen").setTextContent(readBooleanProperty(oldProperties, "keepSendSocketOpen", false));

        String ackTimeout = oldProperties.getProperty("ackTimeout", "5000");
        properties.addChildElement("responseTimeout").setTextContent(ackTimeout);
        properties.addChildElement("ignoreResponse").setTextContent(ackTimeout.equals("0") ? "true" : "false");

        properties.addChildElement("queueOnResponseTimeout").setTextContent(readBooleanProperty(oldProperties, "queueAckTimeout", true));
        properties.addChildElement("processHL7ACK").setTextContent(readBooleanProperty(oldProperties, "processHl7AckResponse", true));
        properties.addChildElement("dataTypeBinary").setTextContent("false");
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("template").setTextContent(oldProperties.getProperty("template", "${message.encodedData}"));
    }

    private static void migrateTCPListenerProperties(DonkeyElement properties) {
        logger.debug("Migrating TCPListenerProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpReceiverProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "6661"));

        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), true);

        DonkeyElement transmissionModeProperties = properties.addChildElement("transmissionModeProperties");
        transmissionModeProperties.setAttribute("class", "com.mirth.connect.model.transmission.framemode.FrameModeProperties");
        transmissionModeProperties.addChildElement("pluginPointName").setTextContent("Basic");
        transmissionModeProperties.addChildElement("startOfMessageBytes").setTextContent("");
        transmissionModeProperties.addChildElement("endOfMessageBytes").setTextContent("");

        properties.addChildElement("serverMode").setTextContent("true");
        properties.addChildElement("reconnectInterval").setTextContent("5000");
        properties.addChildElement("receiveTimeout").setTextContent(oldProperties.getProperty("receiveTimeout", "5000"));
        properties.addChildElement("bufferSize").setTextContent(oldProperties.getProperty("bufferSize", "65536"));
        properties.addChildElement("maxConnections").setTextContent("10");
        properties.addChildElement("keepConnectionOpen").setTextContent(readBooleanProperty(oldProperties, "keepSendSocketOpen", false));
        properties.addChildElement("processBatch").setTextContent("false");
        properties.addChildElement("dataTypeBinary").setTextContent(readBooleanProperty(oldProperties, "binary", false));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("respondOnNewConnection").setTextContent(readBooleanValue(oldProperties, "ackOnNewConnection", false) ? "1" : "0");
        properties.addChildElement("responseAddress").setTextContent(oldProperties.getProperty("ackIP", ""));
        properties.addChildElement("responsePort").setTextContent(oldProperties.getProperty("ackPort", ""));
    }

    private static void migrateTCPSenderProperties(DonkeyElement properties) {
        logger.debug("Migrating TCPSenderProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), oldProperties.getProperty("maxRetryCount"));

        DonkeyElement transmissionModeProperties = properties.addChildElement("transmissionModeProperties");
        transmissionModeProperties.setAttribute("class", "com.mirth.connect.model.transmission.framemode.FrameModeProperties");
        transmissionModeProperties.addChildElement("pluginPointName").setTextContent("Basic");
        transmissionModeProperties.addChildElement("startOfMessageBytes").setTextContent("");
        transmissionModeProperties.addChildElement("endOfMessageBytes").setTextContent("");

        properties.addChildElement("remoteAddress").setTextContent(oldProperties.getProperty("host", "127.0.0.1"));
        properties.addChildElement("remotePort").setTextContent(oldProperties.getProperty("port", "6660"));
        properties.addChildElement("overrideLocalBinding").setTextContent("false");
        properties.addChildElement("localAddress").setTextContent("0.0.0.0");
        properties.addChildElement("localPort").setTextContent("0");
        properties.addChildElement("sendTimeout").setTextContent(oldProperties.getProperty("sendTimeout", "5000"));
        properties.addChildElement("bufferSize").setTextContent(oldProperties.getProperty("bufferSize", "65536"));
        properties.addChildElement("keepConnectionOpen").setTextContent(readBooleanProperty(oldProperties, "keepSendSocketOpen", false));

        String ackTimeout = oldProperties.getProperty("ackTimeout", "5000");
        properties.addChildElement("responseTimeout").setTextContent(ackTimeout);
        properties.addChildElement("ignoreResponse").setTextContent(ackTimeout.equals("0") ? "true" : "false");

        properties.addChildElement("queueOnResponseTimeout").setTextContent("true");
        properties.addChildElement("processHL7ACK").setTextContent("false");
        properties.addChildElement("dataTypeBinary").setTextContent(readBooleanProperty(oldProperties, "binary", false));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("template").setTextContent(oldProperties.getProperty("template", "${message.encodedData}"));
    }

    private static void migrateWebServiceListenerProperties(DonkeyElement properties) {
        logger.debug("Migrating WebServiceListenerProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.ws.WebServiceReceiverProperties");
        properties.removeChildren();

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "8081"));

        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("receiverResponseValue", "None"), true);

        properties.addChildElement("className").setTextContent(oldProperties.getProperty("receiverClassName", "com.mirth.connect.connectors.ws.DefaultAcceptMessage"));
        properties.addChildElement("serviceName").setTextContent(oldProperties.getProperty("receiverServiceName", "Mirth"));
        convertList(properties.addChildElement("usernames"), oldProperties.getProperty("receiverUsernames", ""));
        convertList(properties.addChildElement("passwords"), oldProperties.getProperty("receiverPasswords", ""));
    }

    private static void migrateWebServiceSenderProperties(DonkeyElement properties) {
        logger.debug("Migrating WebServiceSenderProperties");
        dumpElement(properties);
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.ws.WebServiceDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), null);

        properties.addChildElement("wsdlUrl").setTextContent(oldProperties.getProperty("dispatcherWsdlUrl", ""));
        properties.addChildElement("service").setTextContent(oldProperties.getProperty("dispatcherService", ""));
        properties.addChildElement("port").setTextContent(oldProperties.getProperty("dispatcherPort", ""));
        properties.addChildElement("operation").setTextContent(oldProperties.getProperty("dispatcherOperation", "Press Get Operations"));
        properties.addChildElement("useAuthentication").setTextContent(readBooleanProperty(oldProperties, "dispatcherUseAuthentication", false));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("dispatcherUsername", ""));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("dispatcherPassword", ""));
        properties.addChildElement("envelope").setTextContent(oldProperties.getProperty("dispatcherEnvelope", ""));
        properties.addChildElement("oneWay").setTextContent(readBooleanProperty(oldProperties, "dispatcherOneWay", false));
        properties.addChildElement("useMtom").setTextContent(readBooleanProperty(oldProperties, "dispatcherUseMtom", false));
        convertList(properties.addChildElement("attachmentNames"), oldProperties.getProperty("dispatcherAttachmentNames", ""));
        convertList(properties.addChildElement("attachmentContents"), oldProperties.getProperty("dispatcherAttachmentContents", ""));
        convertList(properties.addChildElement("attachmentTypes"), oldProperties.getProperty("dispatcherAttachmentTypes", ""));
        properties.addChildElement("soapAction").setTextContent(oldProperties.getProperty("dispatcherSoapAction", ""));
        properties.addChildElement("wsdlCacheId").setTextContent("");
        convertList(properties.addChildElement("wsdlOperations"), oldProperties.getProperty("dispatcherWsdlOperations", "<list><string>Press Get Operations</string></list>"));
    }

    private static void convertList(DonkeyElement properties, String list) {
        if (StringUtils.isNotEmpty(list)) {
            Matcher matcher = STRING_NODE_PATTERN.matcher(list);
            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    properties.addChildElement("string").setTextContent(matcher.group());
                } else {
                    properties.addChildElement("null");
                }
            }
        }
    }

    private static void migratePollConnectorProperties(DonkeyElement properties, String type, String time, String freq) {
        if (type == null) {
            type = "interval";
        }

        if (freq == null) {
            freq = "5000";
        }

        Calendar timestamp;
        String hour = "0";
        String minute = "0";

        if (time != null && !StringUtils.isBlank(time)) {
            try {
                timestamp = new DateParser().parse(time);
                hour = Integer.toString(timestamp.get(Calendar.HOUR_OF_DAY));
                minute = Integer.toString(timestamp.get(Calendar.MINUTE));
            } catch (DateParserException e) {
            }
        }

        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        properties.addChildElement("pollingType").setTextContent(type);
        properties.addChildElement("pollingHour").setTextContent(hour);
        properties.addChildElement("pollingMinute").setTextContent(minute);
        properties.addChildElement("pollingFrequency").setTextContent(freq);
    }

    private static void buildListenerConnectorProperties(DonkeyElement properties, String host, String port, int defaultPort) {
        if (host == null) {
            host = "0.0.0.0";
        }

        if (port == null) {
            port = Integer.toString(defaultPort);
        }

        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        properties.addChildElement("host").setTextContent(host);
        properties.addChildElement("port").setTextContent(port);
    }

    private static void migrateResponseConnectorProperties(DonkeyElement properties, String responseValue, boolean autoResponseEnabled) {
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        properties.addChildElement("responseVariable").setTextContent(responseValue);

        DonkeyElement defaultQueueOnResponses = properties.addChildElement("defaultQueueOnResponses");
        defaultQueueOnResponses.addChildElement("string").setTextContent("None");

        DonkeyElement defaultQueueOffResponses = properties.addChildElement("defaultQueueOffResponses");
        defaultQueueOffResponses.addChildElement("string").setTextContent("None");

        if (autoResponseEnabled) {
            defaultQueueOnResponses.addChildElement("string").setTextContent("Auto-generate (Before processing)");

            defaultQueueOffResponses.addChildElement("string").setTextContent("Auto-generate (Before processing)");
            defaultQueueOffResponses.addChildElement("string").setTextContent("Auto-generate (After source transformer)");
            defaultQueueOffResponses.addChildElement("string").setTextContent("Auto-generate (Destinations completed)");
            defaultQueueOffResponses.addChildElement("string").setTextContent("Postprocessor");
        }

        properties.addChildElement("respondAfterProcessing").setTextContent("true");
    }

    private static void buildQueueConnectorProperties(DonkeyElement queueConnectorProperties) {
        buildQueueConnectorProperties(queueConnectorProperties, null, null, null, null);
    }

    private static void buildQueueConnectorProperties(DonkeyElement queueConnectorProperties, String queueEnabled, String rotate, String reconnectInterval, String retryCount) {
        if (queueEnabled == null) {
            queueEnabled = "false";
        }

        if (rotate == null) {
            rotate = "false";
        }

        if (reconnectInterval == null) {
            reconnectInterval = "1000";
        }

        if (retryCount == null) {
            retryCount = "0";
        }

        queueConnectorProperties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        queueConnectorProperties.addChildElement("queueEnabled").setTextContent(queueEnabled);
        queueConnectorProperties.addChildElement("sendFirst").setTextContent("false");
        queueConnectorProperties.addChildElement("retryIntervalMillis").setTextContent(reconnectInterval);
        queueConnectorProperties.addChildElement("regenerateTemplate").setTextContent("false");
        queueConnectorProperties.addChildElement("retryCount").setTextContent(retryCount);
        queueConnectorProperties.addChildElement("rotate").setTextContent(rotate);
    }

    private static void migrateDelimitedProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateDICOMProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateEDIProperties(DonkeyElement properties) {
        logger.debug("Migrating EDIDataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.edi.EDIDataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.edi.EDISerializationProperties");
        serializationProperties.addChildElement("segmentDelimiter").setTextContent(oldProperties.getProperty("segmentDelimiter", "~"));
        serializationProperties.addChildElement("elementDelimiter").setTextContent(oldProperties.getProperty("elementDelimiter", "*"));
        serializationProperties.addChildElement("subelementDelimiter").setTextContent(oldProperties.getProperty("subelementDelimiter", ":"));
        serializationProperties.addChildElement("inferX12Delimiters").setTextContent("false");
    }

    private static void migrateHL7v2Properties(DonkeyElement properties) {
        /*
         * Note: 'properties' may be a blank element (when this is called from
         * createDefaultTransformer()). In that case it should be sure to set the appropriate
         * default values for version 3.0.0.
         */

        logger.debug("Migrating HL7v2DataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        // If the data type was HL7 v2.x we set these two properties earlier, so retrieve them now
        String inboundSegmentDelimiter = oldProperties.getProperty("inboundSegmentDelimiter", "\\r\\n|\\r|\\n");
        String outboundSegmentDelimiter = oldProperties.getProperty("outboundSegmentDelimiter", "\\r");

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties");
        serializationProperties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        serializationProperties.addChildElement("handleRepetitions").setTextContent(oldProperties.getProperty("handleRepetitions", "false"));
        serializationProperties.addChildElement("handleSubcomponents").setTextContent(oldProperties.getProperty("handleSubcomponents", "false"));
        serializationProperties.addChildElement("useStrictParser").setTextContent(oldProperties.getProperty("useStrictParser", "false"));
        serializationProperties.addChildElement("useStrictValidation").setTextContent(oldProperties.getProperty("useStrictValidation", "false"));
        serializationProperties.addChildElement("stripNamespaces").setTextContent(oldProperties.getProperty("stripNamespaces", "true"));
        serializationProperties.addChildElement("segmentDelimiter").setTextContent(inboundSegmentDelimiter);

        DonkeyElement deserializationProperties = properties.addChildElement("deserializationProperties");
        deserializationProperties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        deserializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties");
        deserializationProperties.addChildElement("useStrictParser").setTextContent(oldProperties.getProperty("useStrictParser", "false"));
        deserializationProperties.addChildElement("useStrictValidation").setTextContent(oldProperties.getProperty("useStrictValidation", "false"));
        deserializationProperties.addChildElement("segmentDelimiter").setTextContent(outboundSegmentDelimiter);

        DonkeyElement responseGenerationProperties = properties.addChildElement("responseGenerationProperties");
        responseGenerationProperties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        responseGenerationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties");
        responseGenerationProperties.addChildElement("segmentDelimiter").setTextContent(outboundSegmentDelimiter);

        /*
         * These properties would have been set manually in migrateConnector if the source connector
         * was an LLP Listener. Otherwise, just set the defaults.
         */
        responseGenerationProperties.addChildElement("successfulACKCode").setTextContent(oldProperties.getProperty("successfulACKCode", "AA"));
        responseGenerationProperties.addChildElement("successfulACKMessage").setTextContent(oldProperties.getProperty("successfulACKMessage", ""));
        responseGenerationProperties.addChildElement("errorACKCode").setTextContent(oldProperties.getProperty("errorACKCode", "AE"));
        responseGenerationProperties.addChildElement("errorACKMessage").setTextContent(oldProperties.getProperty("errorACKMessage", "An Error Occured Processing Message."));
        responseGenerationProperties.addChildElement("rejectedACKCode").setTextContent(oldProperties.getProperty("rejectedACKCode", "AR"));
        responseGenerationProperties.addChildElement("rejectedACKMessage").setTextContent(oldProperties.getProperty("rejectedACKMessage", "Message Rejected."));
        responseGenerationProperties.addChildElement("msh15ACKAccept").setTextContent(oldProperties.getProperty("msh15ACKAccept", "false"));

        DonkeyElement responseValidationProperties = properties.addChildElement("responseValidationProperties");
        responseValidationProperties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        responseValidationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties");
        responseValidationProperties.addChildElement("successfulACKCode").setTextContent("AA");
        responseValidationProperties.addChildElement("errorACKCode").setTextContent("AE");
        responseValidationProperties.addChildElement("rejectedACKCode").setTextContent("AR");
    }

    private static void migrateHL7v3Properties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateNCPDPProperties(DonkeyElement properties) {
        logger.debug("Migrating NCPDPDataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.ncpdp.NCPDPDataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.ncpdp.NCPDPSerializationProperties");
        serializationProperties.addChildElement("fieldDelimiter").setTextContent(oldProperties.getProperty("fieldDelimiter", "0x1C"));
        serializationProperties.addChildElement("groupDelimiter").setTextContent(oldProperties.getProperty("groupDelimiter", "0x1D"));
        serializationProperties.addChildElement("segmentDelimiter").setTextContent(oldProperties.getProperty("segmentDelimiter", "0x1E"));

        DonkeyElement deserializationProperties = properties.addChildElement("deserializationProperties");
        deserializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.ncpdp.NCPDPDeserializationProperties");
        deserializationProperties.addChildElement("fieldDelimiter").setTextContent(oldProperties.getProperty("fieldDelimiter", "0x1C"));
        deserializationProperties.addChildElement("groupDelimiter").setTextContent(oldProperties.getProperty("groupDelimiter", "0x1D"));
        deserializationProperties.addChildElement("segmentDelimiter").setTextContent(oldProperties.getProperty("segmentDelimiter", "0x1E"));
        deserializationProperties.addChildElement("useStrictValidation").setTextContent(oldProperties.getProperty("useStrictValidation", "false"));
    }

    private static void migrateX12Properties(DonkeyElement properties) {
        logger.debug("Migrating X12DataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.edi.EDIDataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.edi.EDISerializationProperties");
        serializationProperties.addChildElement("segmentDelimiter").setTextContent(oldProperties.getProperty("segmentDelimiter", "~"));
        serializationProperties.addChildElement("elementDelimiter").setTextContent(oldProperties.getProperty("elementDelimiter", "*"));
        serializationProperties.addChildElement("subelementDelimiter").setTextContent(oldProperties.getProperty("subelementDelimiter", ":"));
        serializationProperties.addChildElement("inferX12Delimiters").setTextContent(oldProperties.getProperty("inferX12Delimiters", "true"));
    }

    private static void migrateXMLProperties(DonkeyElement properties) {
        logger.debug("Migrating XMLDataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.xml.XMLDataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.xml.XMLSerializationProperties");
        serializationProperties.addChildElement("stripNamespaces").setTextContent(oldProperties.getProperty("stripNamespaces", "true"));
    }

    private static void createDefaultTransformer(DonkeyElement transformer) {
        transformer.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        transformer.addChildElement("steps");
        transformer.addChildElement("inboundDataType").setTextContent("HL7V2");
        transformer.addChildElement("outboundDataType").setTextContent("HL7V2");

        // migrateHL7v2Properties() will use the default 3.0.0 values when given blank Elements to work with
        migrateHL7v2Properties(transformer.addChildElement("inboundProperties"));
        migrateHL7v2Properties(transformer.addChildElement("outboundProperties"));
    }

    // Utility functions

    private static Properties readPropertiesElement(DonkeyElement propertiesElement) {
        Properties properties = new Properties();

        for (DonkeyElement propertyElement : propertiesElement.getChildElements()) {
            properties.setProperty(propertyElement.getAttribute("name"), propertyElement.getTextContent());
        }

        return properties;
    }

    /**
     * Writes all the entries in the given properties object to the propertiesElement. Any existing
     * elements in propertiesElement will be removed first.
     */
    private static void writePropertiesElement(DonkeyElement propertiesElement, Properties properties) {
        propertiesElement.removeChildren();

        for (Object key : properties.keySet()) {
            DonkeyElement property = propertiesElement.addChildElement("property");
            property.setAttribute("name", key.toString());
            property.setTextContent(properties.getProperty(key.toString()));
        }
    }

    private static String readBooleanProperty(Properties properties, String name) {
        String value = properties.getProperty(name);

        if (value == null) {
            return null;
        } else {
            return readBooleanProperty(properties, name, false);
        }
    }

    private static String readBooleanProperty(Properties properties, String name, boolean defaultValue) {
        return Boolean.toString(readBooleanValue(properties, name, defaultValue));
    }

    private static boolean readBooleanValue(Properties properties, String name, boolean defaultValue) {
        String property = properties.getProperty(name, Boolean.toString(defaultValue));
        return property.equals("1") || Boolean.parseBoolean(property);
    }

    private static void dumpElement(DonkeyElement element) {
        try {
            String xml = XmlUtil.elementToXml(element);
            System.out.println(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
