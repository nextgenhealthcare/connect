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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mirth.connect.donkey.util.DateParser;
import com.mirth.connect.donkey.util.DateParser.DateParserException;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.util.MigrationUtil;

public class ImportConverter3_0_0 {
    private final static String VERSION_ATTRIBUTE_NAME = "version";
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
        if (document.getDocumentElement().hasAttribute(VERSION_ATTRIBUTE_NAME)) {
            return document;
        }

        DonkeyElement root = new DonkeyElement(document.getDocumentElement());

        if (root.getNodeName().equals("list") && (expectedClass == Connector.class || expectedClass == AlertModel.class)) {
            NodeList childNodes = root.getChildNodes();
            int childCount = childNodes.getLength();

            for (int i = 0; i < childCount; i++) {
                Node node = childNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE && !((Element) node).hasAttribute(VERSION_ATTRIBUTE_NAME)) {
                    Element child = (Element) node;

                    if (expectedClass == Connector.class) {
                        Element convertedConnector = MigrationUtil.elementFromXml(ImportConverter.convertConnector(MigrationUtil.elementToXml(child)));
                        migrateConnector(new DonkeyElement(convertedConnector), null);
                        root.replaceChild(document.importNode(convertedConnector, true), child);
                    } else if (expectedClass == AlertModel.class) {
                        migrateAlert(new DonkeyElement(child));
                    }
                }
            }
        } else if (expectedClass == Channel.class) {
            document = new DocumentSerializer().fromXML(ImportConverter.convertChannelString(objectXml));
            migrateChannel(new DonkeyElement(document.getDocumentElement()));
        } else if (expectedClass == Connector.class) {
            document = new DocumentSerializer().fromXML(ImportConverter.convertConnector(objectXml));
            migrateConnector(new DonkeyElement(document.getDocumentElement()), null);
        } else if (expectedClass == AlertModel.class) {
            migrateAlert(root);
        } else if (expectedClass == ChannelProperties.class) {
            migrateChannelProperties(root, false);
            root.setNodeName("channelProperties");
        } else if (expectedClass == CodeTemplate.class) {
            document = ImportConverter.convertCodeTemplates(objectXml);
            migrateCodeTemplate(new DonkeyElement(document.getDocumentElement()));
        } else if (expectedClass == ServerConfiguration.class) {
            document = ImportConverter.convertServerConfiguration(objectXml);
            migrateServerConfiguration(new DonkeyElement(document.getDocumentElement()));
        } else if (expectedClass == Filter.class) {
            document = new DocumentSerializer().fromXML(ImportConverter.convertFilter(objectXml));
            // no 3.0.0 conversion is needed for the Filter class since it didn't change at all in 3.0.0
        }

        return document;
    }

    private static void migrateChannel(DonkeyElement channel) {
        logger.debug("Migrating channel to version " + VERSION_STRING);
        channel.removeChild("version"); // TODO is it safe to remove the version property from the Channel class and other classes?

        // migrate source connector
        DonkeyElement sourceConnector = channel.getChildElement("sourceConnector");
        boolean isDataTypeDICOM = (migrateConnector(sourceConnector, 0).equals("DICOM"));
        DonkeyElement responseConnectorProperties = sourceConnector.getChildElement("properties").getChildElement("responseConnectorProperties");

        // migrate channel properties
        Properties oldProperties = readPropertiesElement(channel.getChildElement("properties"));
        String synchronous = oldProperties.getProperty("synchronous", "true"); // use this later to set "waitForPrevious" on destination connectors
        migrateChannelProperties(channel.getChildElement("properties"), isDataTypeDICOM);

        // migrate destination connectors
        int metaDataId = 1;

        for (DonkeyElement destinationConnector : channel.getChildElement("destinationConnectors").getChildElements()) {
            // Before migrating, we clone the destination connector here in case we need to add a response channel writer later
            DonkeyElement oldDestinationConnector = new DonkeyElement((Element) destinationConnector.cloneNode(true));

            String sendResponseToChannelId = migrateConnector(destinationConnector, metaDataId);
            String destinationName = destinationConnector.getChildElement("name").getTextContent();
            destinationConnector.getChildElement("waitForPrevious").setTextContent(synchronous);

            // Fix response value
            if (responseConnectorProperties != null && destinationName.equals(responseConnectorProperties.getChildElement("responseVariable").getTextContent())) {
                responseConnectorProperties.getChildElement("responseVariable").setTextContent("d" + metaDataId);
            }

            // Add a destination connector if the previous one used the Send Response To option
            if (StringUtils.isNotBlank(sendResponseToChannelId) && !sendResponseToChannelId.equals("sink")) {
                createResponseChannelWriter(oldDestinationConnector, metaDataId++, sendResponseToChannelId);

                // Insert the migrated response channel writer before the next destination connector, or at the end
                destinationConnector.getParentNode().insertBefore(oldDestinationConnector.cloneNode(true), destinationConnector.getNextSibling());
            }

            metaDataId++;
        }

        channel.addChildElement("nextMetaDataId").setTextContent(Integer.toString(metaDataId));
    }

    private static void createResponseChannelWriter(DonkeyElement connector, Integer prevMetaDataId, String channelId) {
        String responseMapKey = "d" + String.valueOf(prevMetaDataId);
        DonkeyElement name = connector.getChildElement("name");
        name.setTextContent(name.getTextContent() + " - Send response to channel " + channelId);

        connector.getChildElement("transportName").setTextContent("Channel Writer");

        // Remove all connector properties and replace them with Channel Writer properties
        DonkeyElement properties = connector.getChildElement("properties");
        properties.removeChildren();
        addChildAndSetName(properties, "DataType").setTextContent("Channel Writer");
        addChildAndSetName(properties, "host").setTextContent(channelId);
        // This connector should always have its destination queue enabled
        addChildAndSetName(properties, "synchronised").setTextContent("0");
        // Sends the response data from the previous destination
        addChildAndSetName(properties, "template").setTextContent("${" + responseMapKey + ".message}");

        // Remove all transformer steps
        DonkeyElement transformer = connector.getChildElement("transformer");
        transformer.getChildElement("steps").removeChildren();
        // We don't want the transformer to run, so set the outbound data type equal to the inbound,
        // and remove all properties (which will cause migration to set them as the defaults)
        transformer.getChildElement("outboundProtocol").setTextContent(transformer.getChildElement("inboundProtocol").getTextContent());
        transformer.getChildElement("inboundProperties").removeChildren();
        transformer.getChildElement("outboundProperties").removeChildren();

        // If the previous destination had a filter, we need to add a filter on this destination as well
        // The filter has a single step that accepts the message if the previous destination was not filtered
        if (connector.getChildElement("filter").getChildElement("rules").getChildElements().size() > 0) {
            // Remove all filter rules
            connector.getChildElement("filter").getChildElement("rules").removeChildren();

            DonkeyElement rule = connector.getChildElement("filter").getChildElement("rules").addChildElement("rule");
            rule.addChildElement("sequenceNumber", "0");
            rule.addChildElement("name", "Accept message if \"$('" + responseMapKey + "').getStatus()\" does not equal 'FILTERED'");
            rule.addChildElement("type", "Rule Builder");
            rule.addChildElement("operator", "NONE");

            DonkeyElement data = rule.addChildElement("data");
            data.setAttribute("class", "map");
            addMapEntry(data, "Values", "'FILTERED'", true);
            addMapEntry(data, "Name", "", false);
            addMapEntry(data, "Equals", "0", false);
            addMapEntry(data, "Field", "$('" + responseMapKey + "').getStatus()", false);
            addMapEntry(data, "OriginalField", "", false);

            rule.addChildElement("script", "if($('" + responseMapKey + "').getStatus() != 'FILTERED')\n{\nreturn true;\n}\nreturn false;");
        }

        // Run the new destination connector through regular migration
        migrateConnector(connector, prevMetaDataId + 1);
    }

    private static void addMapEntry(DonkeyElement map, String key, String value, boolean valueIsList) {
        DonkeyElement entry = map.addChildElement("entry");
        entry.addChildElement("string", key);
        if (valueIsList) {
            entry.addChildElement("list").addChildElement("string", value);
        } else {
            entry.addChildElement("string", value);
        }
    }

    private static String migrateConnector(DonkeyElement connector, Integer metaDataId) {
        logger.debug("Migrating connector");

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

        // convert connector properties
        DonkeyElement transportName = connector.getChildElement("transportName");
        String connectorName = transportName.getTextContent();
        DonkeyElement properties = connector.getChildElement("properties");
        String sendResponseToChannelId = null;

        if (connectorName.equals("Channel Reader")) {
            migrateVmReceiverProperties(properties);
        } else if (connectorName.equals("Channel Writer")) {
            migrateVmDispatcherProperties(properties);
        } else if (connectorName.equals("Database Reader")) {
            migrateDatabaseReceiverProperties(properties);
        } else if (connectorName.equals("Database Writer")) {
            migrateDatabaseDispatcherProperties(properties);
        } else if (connectorName.equals("DICOM Listener")) {
            migrateDICOMReceiverProperties(properties);
        } else if (connectorName.equals("DICOM Sender")) {
            migrateDICOMDispatcherProperties(properties);
        } else if (connectorName.equals("Document Writer")) {
            migrateDocumentDispatcherProperties(properties);
        } else if (connectorName.equals("File Reader")) {
            migrateFileReceiverProperties(properties);
        } else if (connectorName.equals("File Writer")) {
            migrateFileDispatcherProperties(properties);
        } else if (connectorName.equals("HTTP Listener")) {
            migrateHttpReceiverProperties(properties);
        } else if (connectorName.equals("HTTP Sender")) {
            sendResponseToChannelId = migrateHttpDispatcherProperties(properties);
        } else if (connectorName.equals("JavaScript Reader")) {
            migrateJavaScriptReceiverProperties(properties);
        } else if (connectorName.equals("JavaScript Writer")) {
            migrateJavaScriptDispatcherProperties(properties);
        } else if (connectorName.equals("JMS Reader")) {
            transportName.setTextContent("JMS Listener");
            migrateJmsReceiverProperties(properties);
        } else if (connectorName.equals("JMS Writer")) {
            transportName.setTextContent("JMS Sender");
            migrateJmsDispatcherProperties(properties);
        } else if (connectorName.equals("LLP Listener")) {
            transportName.setTextContent("TCP Listener");
            DonkeyElement transformer = connector.getChildElement("transformer");

            // Some properties have been moved from the LLP Listener connector to the HL7 v2.x data type
            if (transformer.getChildElement("inboundProtocol").getTextContent().equals("HL7V2")) {
                Properties connectorProperties = readPropertiesElement(properties);
                DonkeyElement inboundProperties = transformer.getChildElement("inboundProperties");
                if (inboundProperties == null) {
                    inboundProperties = transformer.addChildElement("inboundProperties");
                }

                addChildAndSetName(inboundProperties, "successfulACKCode").setTextContent(connectorProperties.getProperty("ackCodeSuccessful", "AA"));
                addChildAndSetName(inboundProperties, "successfulACKMessage").setTextContent(connectorProperties.getProperty("ackMsgSuccessful", ""));
                addChildAndSetName(inboundProperties, "errorACKCode").setTextContent(connectorProperties.getProperty("ackCodeError", "AE"));
                addChildAndSetName(inboundProperties, "errorACKMessage").setTextContent(connectorProperties.getProperty("ackMsgError", "An Error Occured Processing Message."));
                addChildAndSetName(inboundProperties, "rejectedACKCode").setTextContent(connectorProperties.getProperty("ackCodeRejected", "AR"));
                addChildAndSetName(inboundProperties, "rejectedACKMessage").setTextContent(connectorProperties.getProperty("ackMsgRejected", "Message Rejected."));
                addChildAndSetName(inboundProperties, "msh15ACKAccept").setTextContent(readBooleanProperty(connectorProperties, "checkMSH15", false));
            }

            migrateLLPListenerProperties(properties);
        } else if (connectorName.equals("LLP Sender")) {
            transportName.setTextContent("TCP Sender");
            sendResponseToChannelId = migrateLLPSenderProperties(properties);
        } else if (connectorName.equals("TCP Listener")) {
            migrateTCPListenerProperties(properties);
        } else if (connectorName.equals("TCP Sender")) {
            sendResponseToChannelId = migrateTCPSenderProperties(properties);
        } else if (connectorName.equals("SMTP Sender")) {
            migrateSmtpDispatcherProperties(properties);
        } else if (connectorName.equals("Web Service Listener")) {
            migrateWebServiceListenerProperties(properties);
        } else if (connectorName.equals("Web Service Sender")) {
            sendResponseToChannelId = migrateWebServiceSenderProperties(properties);
        } else if (connectorName.equals("Email Reader")) {
            try {
                Class<?> migratorClass = Class.forName("com.mirth.connect.connectors.email.shared.EmailReceiverMigrate3_0_0");
                Method migrateMethod = migratorClass.getMethod("migrate", DonkeyElement.class);
                migrateMethod.invoke(migratorClass.newInstance(), properties);
            } catch (Exception e) {
                logger.error("Failed to migrate " + connectorName + " properties", e);
            }
        } else {
            logger.error("Failed to migrate properties for unrecognized connector: " + connectorName);
        }

        // convert transformer (no conversion needed for filter since it didn't change at all in 3.0.0)
        String[] dataTypes = migrateTransformer(connector.getChildElement("transformer"));

        // if this is a destination connector, set its response transformer data types to the connector outbound data type.
        if (mode.equals("DESTINATION")) {
            String outboundDataType = dataTypes[1];
            String convertedOutboundDataType;

            if (outboundDataType.equals("EDI") || outboundDataType.equals("X12")) {
                convertedOutboundDataType = "EDI/X12";
            } else {
                convertedOutboundDataType = outboundDataType;
            }

            DonkeyElement responseTransformer = connector.addChildElement("responseTransformer");
            responseTransformer.addChildElement("steps");

            responseTransformer.addChildElement("inboundDataType").setTextContent(convertedOutboundDataType);
            responseTransformer.addChildElement("outboundDataType").setTextContent(convertedOutboundDataType);

            // use the default data type properties.
            migrateDataTypeProperties(responseTransformer.addChildElement("inboundProperties"), outboundDataType);
            migrateDataTypeProperties(responseTransformer.addChildElement("outboundProperties"), outboundDataType);
        }

        // default waitForPrevious to true
        connector.addChildElement("waitForPrevious").setTextContent("true");

        /*
         * Return the inbound data type if source connector, otherwise return the channel id to send
         * the response to.
         */
        if (metaDataId == null) {
            return null;
        } else if (metaDataId == 0) {
            return dataTypes[0];
        } else {
            return sendResponseToChannelId;
        }
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

    private static void migrateChannelProperties(DonkeyElement properties, boolean useDICOMAttachmentHandler) {
        logger.debug("Migrating channel properties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.removeChildren();

        properties.addChildElement("clearGlobalChannelMap").setTextContent(oldProperties.getProperty("clearGlobalChannelMap", "true"));

        if (oldProperties.getProperty("store_messages", "true").equals("true")) {
            properties.addChildElement("messageStorageMode").setTextContent("DEVELOPMENT");
        } else {
            properties.addChildElement("messageStorageMode").setTextContent("DISABLED");
        }

        properties.addChildElement("encryptData").setTextContent(oldProperties.getProperty("encryptData", "false"));
        properties.addChildElement("removeContentOnCompletion").setTextContent(readBooleanProperty(oldProperties, "error_messages_only", false));
        properties.addChildElement("removeAttachmentsOnCompletion").setTextContent(readBooleanProperty(oldProperties, "error_messages_only", false));
        properties.addChildElement("initialStateStarted").setTextContent((oldProperties.getProperty("initialState", "started").equals("started") ? "true" : "false"));
        properties.addChildElement("tags").setAttribute("class", "linked-hash-set");
        properties.addChildElement("metaDataColumns");
        properties.addChildElement("archiveEnabled").setTextContent("true");

        if (useDICOMAttachmentHandler) {
            DonkeyElement attachmentProperties = properties.addChildElement("attachmentProperties");
            attachmentProperties.addChildElement("className").setTextContent("com.mirth.connect.server.attachments.DICOMAttachmentHandler");
            attachmentProperties.addChildElement("type").setTextContent("DICOM");
            attachmentProperties.addChildElement("properties");

            properties.addChildElement("storeAttachments").setTextContent("true");
        } else {
            DonkeyElement attachmentProperties = properties.addChildElement("attachmentProperties");
            attachmentProperties.addChildElement("type").setTextContent("None");
            attachmentProperties.addChildElement("properties");

            properties.addChildElement("storeAttachments").setTextContent("false");
        }

        String maxMessageAge = oldProperties.getProperty("max_message_age");

        if (!StringUtils.isBlank(maxMessageAge) && !maxMessageAge.equals("-1")) {
            properties.addChildElement("pruneMetaDataDays").setTextContent(maxMessageAge);
        }
    }

    private static void migrateCodeTemplate(DonkeyElement codeTemplate) {
        codeTemplate.removeChild("version");
    }

    private static void migrateAlert(DonkeyElement alert) {
        logger.debug("Migrating alert");

        alert.setNodeName("alertModel");

        /*
         * Expression is not migrated because the error codes that are commonly used are no longer
         * valid.
         */
        alert.removeChild("expression");
        // Template and subject are migrated
        String template = alert.removeChild("template").getTextContent();
        String subject = alert.removeChild("subject").getTextContent();

        /*
         * Store all the alert channels before removing the old element.
         */
        DonkeyElement channels = alert.removeChild("channels");
        List<String> channelList = new ArrayList<String>();
        if (channels != null) {
            for (DonkeyElement channel : channels.getChildElements()) {
                channelList.add(channel.getTextContent());
            }
        }

        /*
         * Store all the alert emails before removing the old element.
         */
        DonkeyElement emails = alert.removeChild("emails");
        List<String> emailList = new ArrayList<String>();
        if (emails != null) {
            for (DonkeyElement email : emails.getChildElements()) {
                emailList.add(email.getTextContent());
            }
        }

        /*
         * Add the trigger type element. Migrated alerts will always use the default trigger type.
         */
        DonkeyElement triggerProperties = alert.addChildElement("trigger");
        triggerProperties.setAttribute("class", "defaultTrigger");

        /*
         * Channels created after this alert will not be active for the alert. This matches the
         * pre-3.x behavior.
         */
        DonkeyElement alertChannelsProperties = triggerProperties.addChildElement("alertChannels");
        alertChannelsProperties.addChildElement("newChannel");

        /*
         * Add each channel that was stored. Destinations created after the alert was created WILL
         * be active for the alert. This semi-matches the pre-3.x behavior because alerts were
         * active for a channel only, but might have been filtered based on the connector type.
         */
        DonkeyElement channelsProperties = alertChannelsProperties.addChildElement("channels");
        for (String channelId : channelList) {
            DonkeyElement entryProperties = channelsProperties.addChildElement("entry");
            entryProperties.addChildElement("string").setTextContent(channelId);
            entryProperties.addChildElement("set").addChildElement("null");
        }

        // Add the regex element but don't copy from expression because the error codes are no longer used.
        triggerProperties.addChildElement("regex");

        // Add the actionGroups variables element for the alert.
        DonkeyElement actionGroupsProperties = alert.addChildElement("actionGroups");

        // Add the AlertActionGroup object.
        DonkeyElement alertActionGroupProperties = actionGroupsProperties.addChildElement("alertActionGroup");
        // Add the actions variable for the AlertActionGroup
        DonkeyElement actionsProperties = alertActionGroupProperties.addChildElement("actions");
        /*
         * Add an AlertAction for each stored email address. All pre-3.x alerts only used the EMAIL
         * protocol
         */
        for (String email : emailList) {
            DonkeyElement alertActionProperties = actionsProperties.addChildElement("alertAction");
            alertActionProperties.addChildElement("protocol").setTextContent("EMAIL");
            alertActionProperties.addChildElement("recipient").setTextContent(email);
        }
        // Copy the subject from the old alert.
        alertActionGroupProperties.addChildElement("subject").setTextContent(subject);
        // Copy the template from the old alert.
        alertActionGroupProperties.addChildElement("template").setTextContent(template);
    }

    private static void migrateServerConfiguration(DonkeyElement serverConfiguration) {
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

    private static String[] migrateTransformer(DonkeyElement transformer) {
        logger.debug("Migrating Transformer");

        // TODO make sure that protocol/data type names haven't changed in 3.0.0
        DonkeyElement inboundDataTypeElement = transformer.getChildElement("inboundProtocol");
        DonkeyElement outboundDataTypeElement = transformer.getChildElement("outboundProtocol");
        inboundDataTypeElement.setNodeName("inboundDataType");
        outboundDataTypeElement.setNodeName("outboundDataType");
        String inboundDataType = inboundDataTypeElement.getTextContent();
        String outboundDataType = outboundDataTypeElement.getTextContent();

        DonkeyElement inboundProperties = transformer.getChildElement("inboundProperties");
        DonkeyElement outboundProperties = transformer.getChildElement("outboundProperties");

        /*
         * If both inbound and outbound data types are HL7V2, then the inbound convertLineBreaks
         * needs to be set to true if convertLFtoCR is true on the outbound.
         */
        if (inboundDataType.equals("HL7V2") && outboundDataType.equals("HL7V2")) {
            boolean convertLFtoCROutbound = (outboundProperties == null) ? true : readBooleanValue(readPropertiesElement(outboundProperties), "convertLFtoCR", true);

            if (inboundProperties != null && convertLFtoCROutbound) {
                for (DonkeyElement propertyElement : inboundProperties.getChildElements()) {
                    if (propertyElement.getAttribute("name").equals("convertLFtoCR")) {
                        propertyElement.setTextContent("true");
                    }
                }
            }
        }

        if (inboundProperties == null) {
            inboundProperties = transformer.addChildElement("inboundProperties");
        }

        if (outboundProperties == null) {
            outboundProperties = transformer.addChildElement("outboundProperties");
        }

        migrateDataTypeProperties(inboundProperties, inboundDataType);
        migrateDataTypeProperties(outboundProperties, outboundDataType);

        // Rename EDI and X12 data types to "EDI/X12"
        if (inboundDataType.equals("EDI") || inboundDataType.equals("X12")) {
            inboundDataTypeElement.setTextContent("EDI/X12");
        }
        if (outboundDataType.equals("EDI") || outboundDataType.equals("X12")) {
            outboundDataTypeElement.setTextContent("EDI/X12");
        }

        return new String[] { inboundDataType, outboundDataType };
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
        } else {
            logger.error("Unrecognized data type: " + dataType);
        }
    }

    private static void migrateVmReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating VmReceiverProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.vm.VmReceiverProperties");
        properties.removeChildren();

        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), true);
    }

    private static void migrateVmDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating VmDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.vm.VmDispatcherProperties");
        properties.removeChildren();

        boolean useQueue = !readBooleanValue(oldProperties, "synchronised", false);
        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), useQueue ? "true" : "false", null, null, null);

        String host = oldProperties.getProperty("host", "none");

        if (host.equals("sink")) {
            host = "none";
        }

        properties.addChildElement("channelId").setTextContent(host);
        properties.addChildElement("responseTimeout").setTextContent("0");
        properties.addChildElement("channelTemplate").setTextContent(convertReferences(oldProperties.getProperty("template", "${message.encodedData}")));
    }

    private static void migrateDICOMReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating DICOMReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.dimse.DICOMReceiverProperties");
        properties.removeChildren();

        buildListenerConnectorProperties(properties.addChildElement("listenerConnectorProperties"), oldProperties.getProperty("host"), oldProperties.getProperty("port"), 104);
        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), false);

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

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "127.0.0.1"));
        properties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "104"));
        properties.addChildElement("applicationEntity").setTextContent(oldProperties.getProperty("applicationEntity", ""));
        properties.addChildElement("localHost").setTextContent(oldProperties.getProperty("localHost", ""));
        properties.addChildElement("localPort").setTextContent(oldProperties.getProperty("localPort", ""));
        properties.addChildElement("localApplicationEntity").setTextContent(oldProperties.getProperty("localApplicationEntity", ""));
        properties.addChildElement("template").setTextContent(convertReferences(oldProperties.getProperty("template", "${DICOMMESSAGE}")));
        properties.addChildElement("acceptTo").setTextContent(oldProperties.getProperty("accecptto", "5000"));
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

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));
        properties.addChildElement("host").setTextContent(convertReferences(oldProperties.getProperty("host", "")));
        properties.addChildElement("outputPattern").setTextContent(convertReferences(oldProperties.getProperty("outputPattern", "")));
        properties.addChildElement("documentType").setTextContent(oldProperties.getProperty("documentType", "pdf"));
        properties.addChildElement("encrypt").setTextContent(readBooleanProperty(oldProperties, "encrypt", false));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("password", "")));
        properties.addChildElement("template").setTextContent(convertReferences(oldProperties.getProperty("template", "")));
    }

    private static void migrateFileReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating FileReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.file.FileReceiverProperties");
        properties.removeChildren();

        buildPollConnectorProperties(properties.addChildElement("pollConnectorProperties"), oldProperties.getProperty("pollingType"), oldProperties.getProperty("pollingTime"), oldProperties.getProperty("pollingFrequency"));
        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), "None", true);

        properties.addChildElement("scheme").setTextContent(oldProperties.getProperty("scheme", "file").toUpperCase());
        properties.addChildElement("host").setTextContent(oldProperties.getProperty("host", ""));
        properties.addChildElement("fileFilter").setTextContent(oldProperties.getProperty("fileFilter", "*"));
        properties.addChildElement("regex").setTextContent(readBooleanProperty(oldProperties, "regex", false));
        properties.addChildElement("directoryRecursion").setTextContent("false");
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

        String moveToDirectory = convertReferences(oldProperties.getProperty("moveToDirectory"));
        String moveToFileName = convertReferences(oldProperties.getProperty("moveToPattern"));

        properties.addChildElement("moveToDirectory").setTextContent(moveToDirectory);
        properties.addChildElement("moveToFileName").setTextContent(moveToFileName);

        String afterProcessingAction = "NONE";

        if (Boolean.parseBoolean(oldProperties.getProperty("autoDelete", "false"))) {
            afterProcessingAction = "DELETE";
        } else if (!StringUtils.isBlank(moveToDirectory) || !StringUtils.isBlank(moveToFileName)) {
            afterProcessingAction = "MOVE";
        }

        properties.addChildElement("afterProcessingAction").setTextContent(afterProcessingAction);

        String errorMoveToDirectory = convertReferences(oldProperties.getProperty("moveToErrorDirectory"));
        String errorReadingAction = "NONE";

        if (!StringUtils.isBlank(errorMoveToDirectory)) {
            errorReadingAction = "MOVE";
        }

        properties.addChildElement("errorReadingAction").setTextContent(errorReadingAction);
        properties.addChildElement("errorMoveToDirectory").setTextContent(errorMoveToDirectory);
        properties.addChildElement("errorMoveToFileName").setTextContent("");
        properties.addChildElement("errorResponseAction").setTextContent("AFTER_PROCESSING");
    }

    private static void migrateFileDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating FileDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.file.FileDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("scheme").setTextContent(oldProperties.getProperty("scheme", "file").toUpperCase());
        properties.addChildElement("host").setTextContent(convertReferences(oldProperties.getProperty("host", "")));
        properties.addChildElement("outputPattern").setTextContent(convertReferences(oldProperties.getProperty("outputPattern", "")));
        properties.addChildElement("anonymous").setTextContent(readBooleanProperty(oldProperties, "FTPAnonymous", true));
        properties.addChildElement("username").setTextContent(convertReferences(oldProperties.getProperty("username", "anonymous")));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("password", "anonymous")));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("timeout", "10000"));
        properties.addChildElement("secure").setTextContent(readBooleanProperty(oldProperties, "secure", true));
        properties.addChildElement("passive").setTextContent(readBooleanProperty(oldProperties, "passive", true));
        properties.addChildElement("validateConnection").setTextContent(readBooleanProperty(oldProperties, "validateConnections", true));
        properties.addChildElement("outputAppend").setTextContent(readBooleanProperty(oldProperties, "outputAppend", true));
        properties.addChildElement("errorOnExists").setTextContent(readBooleanProperty(oldProperties, "errorOnExists", false));
        properties.addChildElement("temporary").setTextContent(readBooleanProperty(oldProperties, "temporary", false));
        properties.addChildElement("binary").setTextContent(readBooleanProperty(oldProperties, "binary", false));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("template").setTextContent(convertReferences(oldProperties.getProperty("template", "")));
    }

    private static void migrateHttpReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating HttpReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.http.HttpReceiverProperties");
        properties.removeChildren();

        buildListenerConnectorProperties(properties.addChildElement("listenerConnectorProperties"), oldProperties.getProperty("host"), oldProperties.getProperty("port"), 80);
        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("receiverResponse"), true);

        properties.addChildElement("bodyOnly").setTextContent(readBooleanProperty(oldProperties, "receiverBodyOnly", true));
        properties.addChildElement("responseContentType").setTextContent(oldProperties.getProperty("receiverResponseContentType", "text/plain"));
        properties.addChildElement("responseStatusCode").setTextContent(oldProperties.getProperty("receiverResponseStatusCode", ""));
        properties.addChildElement("charset").setTextContent(oldProperties.getProperty("receiverCharset", "UTF-8"));
        properties.addChildElement("contextPath").setTextContent(oldProperties.getProperty("receiverContextPath", ""));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("receiverTimeout", "0"));

        try {
            convertEscapedText(properties.addChildElement("responseHeaders"), convertReferences(oldProperties.getProperty("receiverResponseHeaders", "&lt;linked-hash-map/&gt;")));
        } catch (DonkeyElementException e) {
            logger.error("Failed to convert HTTP Receiver connection properties", e);
        }
    }

    private static String migrateHttpDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating HttpDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.http.HttpDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), null);

        properties.addChildElement("host").setTextContent(convertReferences(oldProperties.getProperty("host", "")));
        properties.addChildElement("method").setTextContent(oldProperties.getProperty("dispatcherMethod", "post"));
        properties.addChildElement("includeHeadersInResponse").setTextContent(readBooleanProperty(oldProperties, "dispatcherIncludeHeadersInResponse", false));
        properties.addChildElement("multipart").setTextContent(readBooleanProperty(oldProperties, "dispatcherMultipart", false));
        properties.addChildElement("useAuthentication").setTextContent(readBooleanProperty(oldProperties, "dispatcherUseAuthentication", false));
        properties.addChildElement("authenticationType").setTextContent(oldProperties.getProperty("dispatcherAuthenticationType", "Basic"));
        properties.addChildElement("username").setTextContent(convertReferences(oldProperties.getProperty("dispatcherUsername", "")));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("dispatcherPassword", "")));
        properties.addChildElement("content").setTextContent(convertReferences(oldProperties.getProperty("dispatcherContent", "")));
        properties.addChildElement("contentType").setTextContent(oldProperties.getProperty("dispatcherContentType", "text/plain"));
        properties.addChildElement("charset").setTextContent(oldProperties.getProperty("dispatcherCharset", "UTF-8"));
        properties.addChildElement("socketTimeout").setTextContent(convertReferences(oldProperties.getProperty("dispatcherSocketTimeout", "30000")));

        Properties oldHeaderProperties = readPropertiesElement(new DonkeyElement(MigrationUtil.elementFromXml(convertReferences(oldProperties.getProperty("dispatcherHeaders")))));

        DonkeyElement headerProperties = properties.addChildElement("headers");
        headerProperties.setAttribute("class", "linked-hash-map");

        for (Object key : oldHeaderProperties.keySet()) {
            String value = oldHeaderProperties.getProperty((String) key);

            DonkeyElement entry = headerProperties.addChildElement("entry");
            entry.addChildElement("string", (String) key);
            entry.addChildElement("string", value);
        }

        Properties oldParameterProperties = readPropertiesElement(new DonkeyElement(MigrationUtil.elementFromXml(convertReferences(oldProperties.getProperty("dispatcherParameters")))));

        DonkeyElement parameterProperties = properties.addChildElement("parameters");
        parameterProperties.setAttribute("class", "linked-hash-map");

        for (Object key : oldParameterProperties.keySet()) {
            String value = oldParameterProperties.getProperty((String) key);

            DonkeyElement entry = parameterProperties.addChildElement("entry");
            entry.addChildElement("string", (String) key);
            entry.addChildElement("string", value);
        }

        return oldProperties.getProperty("dispatcherReplyChannelId");
    }

    private static void migrateDatabaseReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating DatabaseReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.jdbc.DatabaseReceiverProperties");
        properties.removeChildren();

        buildPollConnectorProperties(properties.addChildElement("pollConnectorProperties"), oldProperties.getProperty("pollingType"), oldProperties.getProperty("pollingTime"), oldProperties.getProperty("pollingFrequency"));
        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"));

        boolean useScript = readBooleanValue(oldProperties, "useScript", false);
        boolean useAck = readBooleanValue(oldProperties, "useAck", false);

        properties.addChildElement("driver").setTextContent(oldProperties.getProperty("driver", "Please Select One"));
        properties.addChildElement("url").setTextContent(oldProperties.getProperty("URL", ""));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("username", ""));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("password", ""));
        properties.addChildElement("select").setTextContent(oldProperties.getProperty(useScript ? "script" : "query", ""));
        properties.addChildElement("update").setTextContent(useScript ? oldProperties.getProperty("ackScript", "") : convertReferences(oldProperties.getProperty("ack", "")));
        properties.addChildElement("useScript").setTextContent(Boolean.toString(useScript));
        properties.addChildElement("cacheResults").setTextContent("true");
        properties.addChildElement("keepConnectionOpen").setTextContent("true");
        properties.addChildElement("updateMode").setTextContent(useAck ? "3" : "1");
        properties.addChildElement("retryCount").setTextContent("3");
        properties.addChildElement("retryInterval").setTextContent("10000");
        properties.addChildElement("fetchSize").setTextContent("1000");
    }

    private static void migrateDatabaseDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating DatabaseDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.jdbc.DatabaseDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        boolean useScript = readBooleanValue(oldProperties, "useScript", false);

        properties.addChildElement("driver").setTextContent(oldProperties.getProperty("driver", "Please Select One"));
        properties.addChildElement("url").setTextContent(convertReferences(oldProperties.getProperty("URL", "")));
        properties.addChildElement("username").setTextContent(convertReferences(oldProperties.getProperty("username", "")));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("password", "")));
        properties.addChildElement("query").setTextContent(useScript ? oldProperties.getProperty("script", "") : convertReferences(oldProperties.getProperty("query", "")));
        properties.addChildElement("useScript").setTextContent(Boolean.toString(useScript));
    }

    private static void migrateJmsReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating JmsReceiverProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.jms.JmsReceiverProperties");
        properties.removeChildren();

        properties.addChildElement("useJndi").setTextContent(readBooleanProperty(oldProperties, "useJndi", false));
        properties.addChildElement("jndiProviderUrl").setTextContent(oldProperties.getProperty("jndiProviderUrl", ""));
        properties.addChildElement("jndiInitialContextFactory").setTextContent(oldProperties.getProperty("jndiInitialFactory", ""));
        properties.addChildElement("jndiConnectionFactoryName").setTextContent(oldProperties.getProperty("connectionFactoryJndiName", ""));
        properties.addChildElement("connectionFactoryClass").setTextContent(oldProperties.getProperty("connectionFactoryClass", ""));
        properties.addChildElement("username").setTextContent(oldProperties.getProperty("username", ""));
        properties.addChildElement("password").setTextContent(oldProperties.getProperty("password", ""));
        properties.addChildElement("destinationName").setTextContent(oldProperties.getProperty("host", ""));
        properties.addChildElement("reconnectIntervalMillis").setTextContent("10000");
        properties.addChildElement("clientId").setTextContent(oldProperties.getProperty("clientId", ""));
        properties.addChildElement("topic").setTextContent(readBooleanProperty(oldProperties, "durable", false));
        properties.addChildElement("durableTopic").setTextContent(readBooleanProperty(oldProperties, "durable", false));
        properties.addChildElement("selector").setTextContent(oldProperties.getProperty("selector", ""));

        DonkeyElement connectionProperties = properties.addChildElement("connectionProperties");
        connectionProperties.setAttribute("class", "linked-hash-map");

        try {
            Properties oldConnectionProperties = readPropertiesElement(new DonkeyElement(oldProperties.getProperty("connectionFactoryProperties")));

            for (Object key : oldConnectionProperties.keySet()) {
                String value = oldConnectionProperties.getProperty((String) key);

                DonkeyElement entry = connectionProperties.addChildElement("entry");
                entry.addChildElement("string", (String) key);
                entry.addChildElement("string", value);
            }
        } catch (DonkeyElementException e) {
            logger.error("Failed to convert JMS Receiver connection properties", e);
        }
    }

    private static void migrateJmsDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating JmsDispatcherProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.jms.JmsDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("useJndi").setTextContent(readBooleanProperty(oldProperties, "useJndi", false));
        properties.addChildElement("jndiProviderUrl").setTextContent(convertReferences(oldProperties.getProperty("jndiProviderUrl", "")));
        properties.addChildElement("jndiInitialContextFactory").setTextContent(oldProperties.getProperty("jndiInitialFactory", ""));
        properties.addChildElement("jndiConnectionFactoryName").setTextContent(oldProperties.getProperty("connectionFactoryJndiName", ""));
        properties.addChildElement("connectionFactoryClass").setTextContent(oldProperties.getProperty("connectionFactoryClass", ""));
        properties.addChildElement("username").setTextContent(convertReferences(oldProperties.getProperty("username", "")));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("password", "")));
        properties.addChildElement("destinationName").setTextContent(convertReferences(oldProperties.getProperty("host", "")));
        properties.addChildElement("reconnectIntervalMillis").setTextContent("10000");
        properties.addChildElement("clientId").setTextContent("");
        properties.addChildElement("topic").setTextContent("false");
        properties.addChildElement("template").setTextContent(convertReferences(oldProperties.getProperty("template", "${message.encodedData}")));

        Properties oldConnectionProperties = readPropertiesElement(new DonkeyElement(MigrationUtil.elementFromXml(oldProperties.getProperty("connectionFactoryProperties"))));

        DonkeyElement connectionProperties = properties.addChildElement("connectionProperties");
        connectionProperties.setAttribute("class", "linked-hash-map");

        for (Object key : oldConnectionProperties.keySet()) {
            String value = convertReferences(oldConnectionProperties.getProperty((String) key));

            DonkeyElement entry = connectionProperties.addChildElement("entry");
            entry.addChildElement("string", (String) key);
            entry.addChildElement("string", value);
        }
    }

    private static void migrateJavaScriptReceiverProperties(DonkeyElement properties) {
        logger.debug("Migrating JavaScriptReceiverProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.js.JavaScriptReceiverProperties");
        properties.removeChildren();

        buildPollConnectorProperties(properties.addChildElement("pollConnectorProperties"), oldProperties.getProperty("pollingType"), oldProperties.getProperty("pollingTime"), oldProperties.getProperty("pollingFrequency"));
        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), false);

        properties.addChildElement("script").setTextContent(oldProperties.getProperty("script", ""));
    }

    private static void migrateJavaScriptDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating JavaScriptDispatcherProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.js.JavaScriptDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("script").setTextContent(oldProperties.getProperty("script", ""));
    }

    private static void migrateSmtpDispatcherProperties(DonkeyElement properties) {
        logger.debug("Migrating SmtpDispatcherProperties");

        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.smtp.SmtpDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"));

        properties.addChildElement("authentication").setTextContent(readBooleanProperty(oldProperties, "authentication", false));
        properties.addChildElement("body").setTextContent(convertReferences(oldProperties.getProperty("body", "")));
        properties.addChildElement("charsetEncoding").setTextContent(oldProperties.getProperty("charsetEncoding", "DEFAULT_ENCODING"));
        properties.addChildElement("encryption").setTextContent(oldProperties.getProperty("encryption", "none"));
        properties.addChildElement("from").setTextContent(oldProperties.getProperty("from", ""));

        properties.addChildElement("html").setTextContent(readBooleanProperty(oldProperties, "html", false));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("password", "")));
        properties.addChildElement("smtpHost").setTextContent(convertReferences(oldProperties.getProperty("smtpHost", "")));
        properties.addChildElement("smtpPort").setTextContent(convertReferences(oldProperties.getProperty("smtpPort", "25")));
        properties.addChildElement("subject").setTextContent(convertReferences(oldProperties.getProperty("subject", "25")));
        properties.addChildElement("timeout").setTextContent(oldProperties.getProperty("timeout", "5000"));
        properties.addChildElement("to").setTextContent(oldProperties.getProperty("to", ""));
        properties.addChildElement("username").setTextContent(convertReferences(oldProperties.getProperty("username", "")));

        properties.addChildElement("cc").setTextContent("");
        properties.addChildElement("bcc").setTextContent("");
        properties.addChildElement("replyTo").setTextContent("");

        try {
            convertEscapedText(properties.addChildElement("headers"), convertReferences(oldProperties.getProperty("headers", "")));
            convertEscapedText(properties.addChildElement("attachments"), convertReferences(oldProperties.getProperty("attachments", "")));
        } catch (DonkeyElementException e) {
            logger.error("Failed to convert SMTP Dispatcher connection properties", e);
        }
    }

    private static void migrateLLPListenerProperties(DonkeyElement properties) {
        logger.debug("Migrating LLPListenerProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpReceiverProperties");
        properties.removeChildren();

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "6661"));

        String responseValue = oldProperties.getProperty("responseValue", "None");
        if (readBooleanValue(oldProperties, "sendACK", true)) {
            responseValue = "Auto-generate (After source transformer)";
        }
        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), responseValue, true);

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

    private static String migrateLLPSenderProperties(DonkeyElement properties) {
        logger.debug("Migrating LLPSenderProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpDispatcherProperties");
        properties.removeChildren();

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

        properties.addChildElement("remoteAddress").setTextContent(convertReferences(oldProperties.getProperty("host", "127.0.0.1")));
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
        properties.addChildElement("template").setTextContent(convertReferences(oldProperties.getProperty("template", "${message.encodedData}")));

        return oldProperties.getProperty("replyChannelId");
    }

    private static void migrateTCPListenerProperties(DonkeyElement properties) {
        logger.debug("Migrating TCPListenerProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpReceiverProperties");
        properties.removeChildren();

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "6661"));

        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("responseValue", "None"), true);

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

    private static String migrateTCPSenderProperties(DonkeyElement properties) {
        logger.debug("Migrating TCPSenderProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.tcp.TcpDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), oldProperties.getProperty("maxRetryCount"));

        DonkeyElement transmissionModeProperties = properties.addChildElement("transmissionModeProperties");
        transmissionModeProperties.setAttribute("class", "com.mirth.connect.model.transmission.framemode.FrameModeProperties");
        transmissionModeProperties.addChildElement("pluginPointName").setTextContent("Basic");
        transmissionModeProperties.addChildElement("startOfMessageBytes").setTextContent("");
        transmissionModeProperties.addChildElement("endOfMessageBytes").setTextContent("");

        properties.addChildElement("remoteAddress").setTextContent(convertReferences(oldProperties.getProperty("host", "127.0.0.1")));
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
        properties.addChildElement("template").setTextContent(convertReferences(oldProperties.getProperty("template", "${message.encodedData}")));

        return oldProperties.getProperty("replyChannelId");
    }

    private static void migrateWebServiceListenerProperties(DonkeyElement properties) {
        logger.debug("Migrating WebServiceListenerProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.ws.WebServiceReceiverProperties");
        properties.removeChildren();

        DonkeyElement listenerConnectorProperties = properties.addChildElement("listenerConnectorProperties");
        listenerConnectorProperties.addChildElement("host").setTextContent(oldProperties.getProperty("host", "0.0.0.0"));
        listenerConnectorProperties.addChildElement("port").setTextContent(oldProperties.getProperty("port", "8081"));

        buildResponseConnectorProperties(properties.addChildElement("responseConnectorProperties"), oldProperties.getProperty("receiverResponseValue", "None"), true);

        properties.addChildElement("className").setTextContent(oldProperties.getProperty("receiverClassName", "com.mirth.connect.connectors.ws.DefaultAcceptMessage"));
        properties.addChildElement("serviceName").setTextContent(oldProperties.getProperty("receiverServiceName", "Mirth"));
        convertList(properties.addChildElement("usernames"), oldProperties.getProperty("receiverUsernames", ""));
        convertList(properties.addChildElement("passwords"), oldProperties.getProperty("receiverPasswords", ""));
    }

    private static String migrateWebServiceSenderProperties(DonkeyElement properties) {
        logger.debug("Migrating WebServiceSenderProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.connectors.ws.WebServiceDispatcherProperties");
        properties.removeChildren();

        buildQueueConnectorProperties(properties.addChildElement("queueConnectorProperties"), readBooleanProperty(oldProperties, "usePersistentQueues"), readBooleanProperty(oldProperties, "rotateQueue"), oldProperties.getProperty("reconnectMillisecs"), null);

        properties.addChildElement("wsdlUrl").setTextContent(convertReferences(oldProperties.getProperty("dispatcherWsdlUrl", "")));
        properties.addChildElement("service").setTextContent(convertReferences(oldProperties.getProperty("dispatcherService", "")));
        properties.addChildElement("port").setTextContent(convertReferences(oldProperties.getProperty("dispatcherPort", "")));
        properties.addChildElement("operation").setTextContent(oldProperties.getProperty("dispatcherOperation", "Press Get Operations"));
        properties.addChildElement("useAuthentication").setTextContent(readBooleanProperty(oldProperties, "dispatcherUseAuthentication", false));
        properties.addChildElement("username").setTextContent(convertReferences(oldProperties.getProperty("dispatcherUsername", "")));
        properties.addChildElement("password").setTextContent(convertReferences(oldProperties.getProperty("dispatcherPassword", "")));
        properties.addChildElement("envelope").setTextContent(convertReferences(oldProperties.getProperty("dispatcherEnvelope", "")));
        properties.addChildElement("oneWay").setTextContent(readBooleanProperty(oldProperties, "dispatcherOneWay", false));
        properties.addChildElement("useMtom").setTextContent(readBooleanProperty(oldProperties, "dispatcherUseMtom", false));
        convertList(properties.addChildElement("attachmentNames"), convertReferences(oldProperties.getProperty("dispatcherAttachmentNames", "")));
        convertList(properties.addChildElement("attachmentContents"), convertReferences(oldProperties.getProperty("dispatcherAttachmentContents", "")));
        convertList(properties.addChildElement("attachmentTypes"), convertReferences(oldProperties.getProperty("dispatcherAttachmentTypes", "")));
        properties.addChildElement("soapAction").setTextContent(convertReferences(oldProperties.getProperty("dispatcherSoapAction", "")));
        properties.addChildElement("wsdlCacheId").setTextContent("");
        convertList(properties.addChildElement("wsdlOperations"), oldProperties.getProperty("dispatcherWsdlOperations", "<list><string>Press Get Operations</string></list>"));

        return oldProperties.getProperty("dispatcherReplyChannelId");
    }

    private static void migrateDelimitedProperties(DonkeyElement properties) {
        logger.debug("Migrating DelimitedDataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.delimited.DelimitedDataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.delimited.DelimitedSerializationProperties");
        serializationProperties.addChildElement("columnDelimiter").setTextContent(oldProperties.getProperty("columnDelimiter", ","));
        serializationProperties.addChildElement("recordDelimiter").setTextContent(escapeString(oldProperties.getProperty("recordDelimiter", "\n")));

        String columnWidths = oldProperties.getProperty("columnWidths");
        if (StringUtils.isNotBlank(columnWidths)) {
            serializationProperties.addChildElement("columnWidths");
            for (String width : columnWidths.split(",")) {
                serializationProperties.getChildElement("columnWidths").addChildElement("int").setTextContent(String.valueOf(NumberUtils.toInt(width, 0)));
            }
        }

        serializationProperties.addChildElement("quoteChar").setTextContent(oldProperties.getProperty("quoteChar", "\""));
        serializationProperties.addChildElement("escapeWithDoubleQuote").setTextContent(oldProperties.getProperty("escapeWithDoubleQuote", "true"));
        serializationProperties.addChildElement("quoteEscapeChar").setTextContent(oldProperties.getProperty("quoteEscapeChar", "\\"));

        String columnNames = oldProperties.getProperty("columnNames");
        if (StringUtils.isNotBlank(columnNames)) {
            serializationProperties.addChildElement("columnNames");
            for (String name : columnNames.split(",")) {
                serializationProperties.getChildElement("columnNames").addChildElement("string").setTextContent(name);
            }
        }

        serializationProperties.addChildElement("numberedRows").setTextContent(oldProperties.getProperty("numberedRows", "false"));
        serializationProperties.addChildElement("ignoreCR").setTextContent(oldProperties.getProperty("ignoreCR", "true"));

        DonkeyElement deserializationProperties = properties.addChildElement("deserializationProperties");
        deserializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.delimited.DelimitedDeserializationProperties");
        deserializationProperties.addChildElement("columnDelimiter").setTextContent(oldProperties.getProperty("columnDelimiter", ","));
        deserializationProperties.addChildElement("recordDelimiter").setTextContent(escapeString(oldProperties.getProperty("recordDelimiter", "\n")));

        if (StringUtils.isNotBlank(columnWidths)) {
            deserializationProperties.addChildElement("columnWidths");
            for (String width : columnWidths.split(",")) {
                deserializationProperties.getChildElement("columnWidths").addChildElement("int").setTextContent(String.valueOf(NumberUtils.toInt(width, 0)));
            }
        }

        deserializationProperties.addChildElement("quoteChar").setTextContent(oldProperties.getProperty("quoteChar", "\""));
        deserializationProperties.addChildElement("escapeWithDoubleQuote").setTextContent(oldProperties.getProperty("escapeWithDoubleQuote", "true"));
        deserializationProperties.addChildElement("quoteEscapeChar").setTextContent(oldProperties.getProperty("quoteEscapeChar", "\\"));

        DonkeyElement batchProperties = properties.addChildElement("batchProperties");
        batchProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.delimited.DelimitedBatchProperties");
        batchProperties.addChildElement("batchSkipRecords").setTextContent(oldProperties.getProperty("batchSkipRecords", "0"));
        batchProperties.addChildElement("batchSplitByRecord").setTextContent(oldProperties.getProperty("batchSplitByRecord", "true"));
        batchProperties.addChildElement("batchMessageDelimiter").setTextContent(oldProperties.getProperty("batchMessageDelimiter", ""));
        batchProperties.addChildElement("batchMessageDelimiterIncluded").setTextContent(oldProperties.getProperty("batchMessageDelimiterIncluded", "false"));
        batchProperties.addChildElement("batchGroupingColumn").setTextContent(oldProperties.getProperty("batchGroupingColumn", ""));
        batchProperties.addChildElement("batchScript").setTextContent(oldProperties.getProperty("batchScript", ""));
    }

    private static String escapeString(String str) {
        return str.replace("\b", "\\b").replace("\t", "\\t").replace("\n", "\\n").replace("\f", "\\f").replace("\r", "\\r");
    }

    private static void migrateDICOMProperties(DonkeyElement properties) {
        logger.debug("Migrating DICOMDataTypeProperties");
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.dicom.DICOMDataTypeProperties");
        properties.removeChildren();
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
         * Note: 'properties' may be a blank element. In that case it should be sure to set the
         * appropriate default values for version 3.0.0.
         */

        logger.debug("Migrating HL7v2DataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties");
        serializationProperties.addChildElement("handleRepetitions").setTextContent(oldProperties.getProperty("handleRepetitions", "false"));
        serializationProperties.addChildElement("handleSubcomponents").setTextContent(oldProperties.getProperty("handleSubcomponents", "false"));
        serializationProperties.addChildElement("useStrictParser").setTextContent(oldProperties.getProperty("useStrictParser", "false"));
        serializationProperties.addChildElement("useStrictValidation").setTextContent(oldProperties.getProperty("useStrictValidation", "false"));
        serializationProperties.addChildElement("stripNamespaces").setTextContent(oldProperties.getProperty("stripNamespaces", "true"));
        serializationProperties.addChildElement("segmentDelimiter").setTextContent("\\r");
        serializationProperties.addChildElement("convertLineBreaks").setTextContent(oldProperties.getProperty("convertLFtoCR", "true"));

        DonkeyElement deserializationProperties = properties.addChildElement("deserializationProperties");
        deserializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties");
        deserializationProperties.addChildElement("useStrictParser").setTextContent(oldProperties.getProperty("useStrictParser", "false"));
        deserializationProperties.addChildElement("useStrictValidation").setTextContent(oldProperties.getProperty("useStrictValidation", "false"));
        deserializationProperties.addChildElement("segmentDelimiter").setTextContent("\\r");

        DonkeyElement responseGenerationProperties = properties.addChildElement("responseGenerationProperties");
        responseGenerationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties");
        responseGenerationProperties.addChildElement("segmentDelimiter").setTextContent("\\r");

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
        responseValidationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties");
        responseValidationProperties.addChildElement("successfulACKCode").setTextContent("AA");
        responseValidationProperties.addChildElement("errorACKCode").setTextContent("AE");
        responseValidationProperties.addChildElement("rejectedACKCode").setTextContent("AR");
    }

    private static void migrateHL7v3Properties(DonkeyElement properties) {
        logger.debug("Migrating HL7v3DataTypeProperties");
        Properties oldProperties = readPropertiesElement(properties);
        properties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v3.HL7V3DataTypeProperties");
        properties.removeChildren();

        DonkeyElement serializationProperties = properties.addChildElement("serializationProperties");
        serializationProperties.setAttribute("class", "com.mirth.connect.plugins.datatypes.hl7v3.HL7V3SerializationProperties");
        serializationProperties.addChildElement("stripNamespaces").setTextContent(oldProperties.getProperty("stripNamespaces", "true"));
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

    /*
     * Utility Methods
     * 
     * NOTE: These public utility methods may be referenced by private plugins that have 3.0.0
     * migration code.
     */

    public static void buildPollConnectorProperties(DonkeyElement properties, String type, String time, String freq) {
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

        properties.addChildElement("pollingType").setTextContent(type);
        properties.addChildElement("pollingHour").setTextContent(hour);
        properties.addChildElement("pollingMinute").setTextContent(minute);
        properties.addChildElement("pollingFrequency").setTextContent(freq);
    }

    public static void buildListenerConnectorProperties(DonkeyElement properties, String host, String port, int defaultPort) {
        if (host == null) {
            host = "0.0.0.0";
        }

        if (port == null) {
            port = Integer.toString(defaultPort);
        }

        properties.addChildElement("host").setTextContent(host);
        properties.addChildElement("port").setTextContent(port);
    }

    public static void buildResponseConnectorProperties(DonkeyElement properties) {
        buildResponseConnectorProperties(properties, "None", false);
    }

    public static void buildResponseConnectorProperties(DonkeyElement properties, String responseValue, boolean autoResponseEnabled) {
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

    public static void buildQueueConnectorProperties(DonkeyElement queueConnectorProperties) {
        buildQueueConnectorProperties(queueConnectorProperties, null, null, null, null);
    }

    public static void buildQueueConnectorProperties(DonkeyElement queueConnectorProperties, String queueEnabled, String rotate, String reconnectInterval, String retryCount) {
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

        queueConnectorProperties.addChildElement("queueEnabled").setTextContent(queueEnabled);
        queueConnectorProperties.addChildElement("sendFirst").setTextContent("false");
        queueConnectorProperties.addChildElement("retryIntervalMillis").setTextContent(reconnectInterval);
        queueConnectorProperties.addChildElement("regenerateTemplate").setTextContent("false");
        queueConnectorProperties.addChildElement("retryCount").setTextContent(retryCount);
        queueConnectorProperties.addChildElement("rotate").setTextContent(rotate);
    }

    public static Properties readPropertiesElement(DonkeyElement propertiesElement) {
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
    public static void writePropertiesElement(DonkeyElement propertiesElement, Properties properties) {
        propertiesElement.removeChildren();

        for (Object key : properties.keySet()) {
            DonkeyElement property = propertiesElement.addChildElement("property");
            property.setAttribute("name", key.toString());
            property.setTextContent(properties.getProperty(key.toString()));
        }
    }

    public static String readBooleanProperty(Properties properties, String name) {
        String value = properties.getProperty(name);

        if (value == null) {
            return null;
        } else {
            return readBooleanProperty(properties, name, false);
        }
    }

    public static String readBooleanProperty(Properties properties, String name, boolean defaultValue) {
        return Boolean.toString(readBooleanValue(properties, name, defaultValue));
    }

    public static boolean readBooleanValue(Properties properties, String name, boolean defaultValue) {
        String property = properties.getProperty(name, Boolean.toString(defaultValue));
        return property.equals("1") || Boolean.parseBoolean(property);
    }

    public static void convertList(DonkeyElement properties, String list) {
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

    public static void convertEscapedText(DonkeyElement newProperties, String list) throws DonkeyElementException {
        if (StringUtils.isNotEmpty(list)) {
            DonkeyElement oldProperties = new DonkeyElement(DonkeyElement.elementFromXml(list));

            for (DonkeyElement oldProperty : oldProperties.getChildElements()) {
                newProperties.appendChild(newProperties.getOwnerDocument().importNode(oldProperty.getElement(), true));
            }
        }
    }

    public static String convertReferences(String reference) {
        reference = reference.replace("${MESSAGEATTACH}", "${message.encodedData}");
        reference = reference.replace("$!{MESSAGEATTACH}", "$!{message.encodedData}");

        reference = reference.replace("${ORIGINALNAME}", "${originalFilename}");
        reference = reference.replace("$!{ORIGINALNAME}", "$!{originalFilename}");

        return reference;
    }

    private static void dumpElement(DonkeyElement element) {
        try {
            String xml = MigrationUtil.elementToXml(element);
            System.out.println(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
