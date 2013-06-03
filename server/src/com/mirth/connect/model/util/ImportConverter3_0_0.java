/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import java.util.Calendar;
import java.util.Properties;

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
        migrateConnector(channel.getChildElement("sourceConnector"), 0);

        // migrate destination connectors
        int metaDataId = 1;

        for (DonkeyElement destinationConnector : channel.getChildElement("destinationConnectors").getChildElements()) {
            migrateConnector(destinationConnector, metaDataId++);
            destinationConnector.getChildElement("waitForPrevious").setTextContent(synchronous);
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
            migrateLLPReceiverProperties(properties);
        } else if (dataType.equals("LLP Sender")) {
            migrateLLPDispatcherProperties(properties);
        } else if (dataType.equals("TCP Listener")) {
            migrateTcpReceiverProperties(properties);
        } else if (dataType.equals("TCP Sender")) {
            migrateTcpDispatcherProperties(properties);
        } else if (dataType.equals("SMTP Sender")) {
            migrateSmtpDispatcherProperties(properties);
        } else if (dataType.equals("Web Service Listener")) {
            migrateWebServiceReceiverProperties(properties);
        } else if (dataType.equals("Web Service Sender")) {
            migrateWebServiceDispatcherProperties(properties);
        }
        
        // convert transformer (no conversion needed for filter since it didn't change at all in 3.0.0)
        migrateTransformer(connector.getChildElement("transformer"));

        // default waitForPrevious to true
        connector.addChildElement("waitForPrevious").setTextContent("true");
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
        
        if (inboundProperties != null) {
            migrateDataTypeProperties(inboundProperties, inboundDataType.getTextContent());
        }
        
        if (outboundProperties != null) {
            migrateDataTypeProperties(outboundProperties, outboundDataType.getTextContent());
        }
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

        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"));
    }

    private static void migrateVmDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating VmDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.vm.VmDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), null, null, null);

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

        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"));
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
        
        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), null, null, null);

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
        
        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), null, null, null);
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
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), "");
        
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

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), null, null, null);
        
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
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("receiverResponse"));
        
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

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("queuePollInterval"));

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
        migrateResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"));
    
        properties.addChildElement("script").setTextContent(oldProperties.getProperty("script", ""));
    }
    
    private static void migrateJavaScriptDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating JavaScriptDispatcherProperties");
        
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.js.JavaScriptDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), null, null, null);
        
        properties.addChildElement("script").setTextContent(oldProperties.getProperty("script", ""));
    }
    
    private static void migrateSmtpDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating SmtpDispatcherProperties");
        
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.smtp.SmtpDispatcherProperties");
        properties.removeChildren();
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), null, null, null);
        
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
    
    private static void migrateLLPReceiverProperties(DonkeyElement properties) {
        // TODO
    }
    
    private static void migrateLLPDispatcherProperties(DonkeyElement properties) {
        // TODO
    }
    
    private static void migrateTcpReceiverProperties(DonkeyElement properties) {
        // TODO
    }
    
    private static void migrateTcpDispatcherProperties(DonkeyElement properties) {
        // TODO
    }
    
    private static void migrateWebServiceReceiverProperties(DonkeyElement properties) {
        // TODO
    }
    
    private static void migrateWebServiceDispatcherProperties(DonkeyElement properties) {
        // TODO
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

    private static void migrateResponseConnectorProperties(DonkeyElement properties, String responseValue) {
        properties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        properties.addChildElement("responseVariable").setTextContent(responseValue);

        DonkeyElement defaultQueueOnResponses = properties.addChildElement("defaultQueueOnResponses");
        defaultQueueOnResponses.addChildElement("string").setTextContent("None");
        defaultQueueOnResponses.addChildElement("string").setTextContent("Auto-generate (Before processing)");

        DonkeyElement defaultQueueOffResponses = properties.addChildElement("defaultQueueOffResponses");
        defaultQueueOffResponses.addChildElement("string").setTextContent("None");
        defaultQueueOffResponses.addChildElement("string").setTextContent("Auto-generate (Before processing)");
        defaultQueueOffResponses.addChildElement("string").setTextContent("Auto-generate (After source transformer)");
        defaultQueueOffResponses.addChildElement("string").setTextContent("Auto-generate (Destinations completed)");
        defaultQueueOffResponses.addChildElement("string").setTextContent("Postprocessor");

        properties.addChildElement("respondAfterProcessing").setTextContent("true");
    }

    private static void buildQueueConnectorProperties(DonkeyElement queueConnectorProperties, String queueEnabled, String rotate, String reconnectInterval) {
        if (queueEnabled == null) {
            queueEnabled = "false";
        }
        
        if (rotate == null) {
            rotate = "false";
        }
        
        if (reconnectInterval == null) {
            reconnectInterval = "1000";
        }
        
        queueConnectorProperties.setAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, VERSION_STRING);
        queueConnectorProperties.addChildElement("queueEnabled").setTextContent(queueEnabled);
        queueConnectorProperties.addChildElement("sendFirst").setTextContent("false");
        queueConnectorProperties.addChildElement("retryIntervalMillis").setTextContent(reconnectInterval);
        queueConnectorProperties.addChildElement("regenerateTemplate").setTextContent("false");
        queueConnectorProperties.addChildElement("retryCount").setTextContent("0");
        queueConnectorProperties.addChildElement("rotate").setTextContent(rotate);
    }
    
    private static void migrateDelimitedProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateDICOMProperties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateEDIProperties(DonkeyElement properties) {
        // TODO
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

        String convertLFtoCR = oldProperties.getProperty("convertLFtoCR", "true");
        String inboundSegmentDelimiter = "\\r\\n|\\r|\\n";
        String outboundSegmentDelimiter = convertLFtoCR.equals("true") ? "\\r" : "\\r\\n|\\r|\\n"; // TODO check to make sure this is correct

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
        responseGenerationProperties.addChildElement("successfulACKCode").setTextContent("AA");
        responseGenerationProperties.addChildElement("successfulACKMessage").setTextContent("");
        responseGenerationProperties.addChildElement("errorACKCode").setTextContent("AE");
        responseGenerationProperties.addChildElement("errorACKMessage").setTextContent("An Error Occured Processing Message.");
        responseGenerationProperties.addChildElement("rejectedACKCode").setTextContent("AR");
        responseGenerationProperties.addChildElement("rejectedACKMessage").setTextContent("Message Rejected.");
        responseGenerationProperties.addChildElement("msh15ACKAccept").setTextContent("false");

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
        // TODO
    }

    private static void migrateX12Properties(DonkeyElement properties) {
        // TODO
    }

    private static void migrateXMLProperties(DonkeyElement properties) {
        // TODO
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
     * Writes all the entries in the given properties object to the propertiesElement. Any existing elements in propertiesElement will be removed first.
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
            return Boolean.toString(Boolean.parseBoolean(value));
        }
    }
    
    private static String readBooleanProperty(Properties properties, String name, boolean defaultValue) {
        return Boolean.toString(Boolean.parseBoolean(properties.getProperty(name, Boolean.toString(defaultValue))));
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
