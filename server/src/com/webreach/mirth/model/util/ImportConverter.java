package com.webreach.mirth.model.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class ImportConverter
{
    private enum Direction
    {
        INBOUND, OUTBOUND
    }

    private enum Mode
    {
        ROUTER, BROADCAST
    }
    private static String read(File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder contents = new StringBuilder();
        String line = null;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                contents.append(line + "\n");
            }
        }
        finally
        {
            reader.close();
        }

        return contents.toString();
    }
    /** Removes certain invalid characters
    */
    public static String removeInvalidHexChar(String string) {
	    String result = string;
	    for (char i = 0x0; i <= 0x8; i++) {
	    	result = result.replace(i, ' ');
	    }
	    for (char i = 0xB; i <= 0xC; i++) {
	    	result = result.replace(i, ' ');
	    }
	    for (char i = 0xE; i <= 0x1F; i++) {
	    	result = result.replace(i, ' ');
	    }
	    return result;
    }
    
    /*
     * Method used to convert messages from one version to another.
     * Right now this method does nothing more than returning the string contents.
     */
    public static String convertMessage(String message) throws Exception
    {
        return message;
    }
    
    public static Channel convertChannelObject(Channel channel) throws Exception
    {
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String channelXML = removeInvalidHexChar(serializer.toXML(channel));
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(channelXML)));
        
        return (Channel) serializer.fromXML(convertChannel(document));
    }
    
    public static String convertChannelFile(File channel) throws Exception
    {
        String contents = removeInvalidHexChar(read(channel));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(contents)));
        
        return convertChannel(document);
    }
    
    /*
     * Upgrade pre-1.4 channels to work with 1.4+
     */
    public static String convertChannel(Document document) throws Exception
    {
        String channelXML = "";
        Element channelRoot = document.getDocumentElement();
        
        String version = channelRoot.getElementsByTagName("version").item(0).getTextContent();
        
        int majorVersion = Integer.parseInt(version.split("\\.")[0]);
        int minorVersion = Integer.parseInt(version.split("\\.")[1]);
        int patchVersion = Integer.parseInt(version.split("\\.")[2]);
        
        Direction direction = null;
        Mode mode = null;

        if (channelRoot.getElementsByTagName("direction").getLength() > 0)
        {
            Node channelDirection = channelRoot.getElementsByTagName("direction").item(0);

            if (channelDirection.getTextContent().equals("INBOUND"))
                direction = Direction.INBOUND;
            else if (channelDirection.getTextContent().equals("OUTBOUND"))
                direction = Direction.OUTBOUND;

            channelRoot.removeChild(channelDirection);
        }

        NodeList modeElements = channelRoot.getElementsByTagName("mode");
        if (modeElements.getLength() > 0)
        {
            for (int i = 0; i < modeElements.getLength(); i++)
            {
                if (((Element) modeElements.item(i)).getParentNode() == channelRoot)
                {
                    Node channelMode = modeElements.item(i);

                    if (channelMode.getTextContent().equals("BROADCAST"))
                        mode = Mode.BROADCAST;
                    else if (channelMode.getTextContent().equals("ROUTER"))
                        mode = Mode.ROUTER;

                    channelRoot.removeChild(channelMode);
                }
            }
        }

        if (channelRoot.getElementsByTagName("protocol").getLength() > 0)
        {
            Node channelProtocol = channelRoot.getElementsByTagName("protocol").item(0);
            channelRoot.removeChild(channelProtocol);
        }
        
        if (channelRoot.getElementsByTagName("deployScript").getLength() == 0)
        {
            Element deployScript = document.createElement("deployScript");
            deployScript.setTextContent("// This script executes once when the mule engine is started\r\n// You only have access to the globalMap here to persist data\r\nreturn;");
            channelRoot.appendChild(deployScript);
        }
        
        if (channelRoot.getElementsByTagName("shutdownScript").getLength() == 0)
        {
            Element shutdownScript = document.createElement("shutdownScript");
            shutdownScript.setTextContent("// This script executes once when the mule engine is stopped\r\n// You only have access to the globalMap here to persist data\r\nreturn;");
            channelRoot.appendChild(shutdownScript);
        }
        
        if (channelRoot.getElementsByTagName("postprocessorScript").getLength() == 0)
        {
            Element postprocessorScript = document.createElement("postprocessorScript");
            postprocessorScript.setTextContent("// This script executes once after a message has been processed\r\nreturn;");
            channelRoot.appendChild(postprocessorScript);
        }

        NodeList transportNames = channelRoot.getElementsByTagName("transportName");
        for (int i = 0; i < transportNames.getLength(); i++)
        {
            if (transportNames.item(i).getTextContent().equals("PDF Writer"))
            {
                transportNames.item(i).setTextContent("Document Writer");
            }
        }

        NodeList properyNames = channelRoot.getElementsByTagName("property");
        for (int i = 0; i < properyNames.getLength(); i++)
        {
            Node nameAttribute = properyNames.item(i).getAttributes().getNamedItem("name");
            if (properyNames.item(i).getAttributes().getLength() > 0 && nameAttribute != null)
            {
                if (nameAttribute.getNodeValue().equals("DataType"))
                {
                    if (properyNames.item(i).getTextContent().equals("PDF Writer"))
                    {
                        properyNames.item(i).setTextContent("Document Writer");
                    }
                }
            }
        }

        if (direction == Direction.OUTBOUND)
        {
            Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
            document = getUpdatedTransformer(document, Direction.OUTBOUND, sourceConnectorRoot);

            Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);

            NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");

            for (int i = 0; i < destinationsConnectors.getLength(); i++)
            {
                document = getUpdatedTransformer(document, Direction.OUTBOUND, (Element) destinationsConnectors.item(i));
            }
        }
        else if (direction == Direction.INBOUND)
        {
            if (mode == Mode.BROADCAST)
            {
                Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                document = getUpdatedTransformer(document, Direction.INBOUND, sourceConnectorRoot);

                Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);

                NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");

                for (int i = 0; i < destinationsConnectors.getLength(); i++)
                {
                    document = getUpdatedTransformer(document, Direction.INBOUND, (Element) destinationsConnectors.item(i));
                }
            }
            else if (mode == Mode.ROUTER)
            {
                Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                document = getUpdatedTransformer(document, Direction.INBOUND, sourceConnectorRoot);

                Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);

                NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");

                for (int i = 0; i < destinationsConnectors.getLength(); i++)
                {
                    document = getUpdatedTransformer(document, Direction.INBOUND, (Element) destinationsConnectors.item(i));
                }
            }
        }        
        
        if(minorVersion < 5)
        {
            updateTransformerFor1_5(document);
        }
        
        if(minorVersion < 6)
        {
            // Go through each connector and set it to enabled if that property doesn't exist.
            Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
            
            if (sourceConnectorRoot.getElementsByTagName("enabled").getLength() == 0)
            {
                Element enabled = document.createElement("enabled");
                enabled.setTextContent("true");
                sourceConnectorRoot.appendChild(enabled);
            }
            else
            {
                Element enabled = (Element)sourceConnectorRoot.getElementsByTagName("enabled").item(0);
                enabled.setTextContent("true");
            }
            
            Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);
            NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");
            
            for (int i = 0; i < destinationsConnectors.getLength(); i++)
            {
                Element destinationConnector = (Element)destinationsConnectors.item(i);
                if (destinationConnector.getElementsByTagName("enabled").getLength() == 0)
                {
                    Element enabled = document.createElement("enabled");
                    enabled.setTextContent("true");
                    destinationConnector.appendChild(enabled);
                }
                else
                {
                    Element enabled = (Element)destinationConnector.getElementsByTagName("enabled").item(0);
                    enabled.setTextContent("true");
                }
            }
        }

        DocumentSerializer docSerializer = new DocumentSerializer();
        channelXML = docSerializer.toXML(document);

        return updateLocalAndGlobalVariables(channelXML);
    }

    /*
     * Upgrade pre-1.4 channels' transformers and filters to work with 1.4+
     */
    private static Document getUpdatedTransformer(Document document, Direction direction, Element connector)
    {
        Element filterElement = (Element) connector.getElementsByTagName("filter").item(0);
        Element filterTemplate = null;

        if (filterElement.getElementsByTagName("template").getLength() > 0)
        {
            filterTemplate = (Element) filterElement.getElementsByTagName("template").item(0);
            if (filterTemplate != null)
                filterElement.removeChild(filterElement.getElementsByTagName("template").item(0));
        }

        Element transformerElement = (Element) connector.getElementsByTagName("transformer").item(0);
        String template = null;
        Element transformerTemplate = null;

        if (transformerElement.getElementsByTagName("template").getLength() > 0)
        {
            transformerTemplate = (Element) transformerElement.getElementsByTagName("template").item(0);
            if (transformerTemplate != null)
                template = transformerTemplate.getTextContent();
        }

        Element inboundTemplateElement = document.createElement("inboundTemplate");
        Element outboundTemplateElement = document.createElement("outboundTemplate");
        Element inboundProtocolElement = document.createElement("inboundProtocol");
        Element outboundProtocolElement = document.createElement("outboundProtocol");

        Element modeElement = document.createElement("mode");

        if (direction == Direction.OUTBOUND && connector.getTagName().equals("sourceConnector"))
        {
            inboundProtocolElement.setTextContent(MessageObject.Protocol.XML.toString());
            outboundProtocolElement.setTextContent(MessageObject.Protocol.XML.toString());
        }
        else if (direction == Direction.OUTBOUND && connector.getTagName().equals("com.webreach.mirth.model.Connector"))
        {
            inboundProtocolElement.setTextContent(MessageObject.Protocol.XML.toString());
            outboundProtocolElement.setTextContent(MessageObject.Protocol.HL7V2.toString());
            if (template != null)
                outboundTemplateElement.setTextContent(template);
        }
        else if (direction == Direction.INBOUND)
        {
            inboundProtocolElement.setTextContent(MessageObject.Protocol.HL7V2.toString());
            outboundProtocolElement.setTextContent(MessageObject.Protocol.HL7V2.toString());
            if (template != null)
                inboundTemplateElement.setTextContent(template);
        }

        if (connector.getTagName().equals("sourceConnector"))
            modeElement.setTextContent(Connector.Mode.SOURCE.toString());
        else
            modeElement.setTextContent(Connector.Mode.DESTINATION.toString());

        transformerElement.appendChild(inboundTemplateElement);
        transformerElement.appendChild(outboundTemplateElement);
        transformerElement.appendChild(inboundProtocolElement);
        transformerElement.appendChild(outboundProtocolElement);

        // replace HL7 Message builder with Message Builder
        NodeList steps = transformerElement.getElementsByTagName("com.webreach.mirth.model.Step");

        for (int i = 0; i < steps.getLength(); i++)
        {
            Element step = (Element) steps.item(i);
            NodeList stepTypesList = step.getElementsByTagName("type");
            if (stepTypesList.getLength() > 0)
            {
                Element stepType = (Element) stepTypesList.item(0);
                if (stepType.getTextContent().equals("HL7 Message Builder"))
                {
                    stepType.setTextContent("Message Builder");
                }

                if (stepType.getTextContent().equals("Message Builder") || stepType.getTextContent().equals("Mapper"))
                {
                    boolean foundRegex = false, foundDefaultValue = false;
                    Element data = (Element) step.getElementsByTagName("data").item(0);
                    NodeList entries = data.getElementsByTagName("entry");
                    for (int j = 0; j < entries.getLength(); j++)
                    {
                        NodeList strings = ((Element)entries.item(j)).getElementsByTagName("string");
                        
                        if (strings.getLength() > 0)
                        {
                            if (strings.item(0).getTextContent().equals("RegularExpressions"))
                                foundRegex = true;
                            else if (strings.item(0).getTextContent().equals("DefaultValue"))
                                foundDefaultValue = true;
                            
                            if(strings.item(0).getTextContent().equals("isGlobal"))
                            {
                                if(strings.item(1).getTextContent().equals("0"))
                                    strings.item(1).setTextContent("channel");
                                else if(strings.item(1).getTextContent().equals("1"))
                                    strings.item(1).setTextContent("global");
                            }
                        }
                    }

                    if (!foundRegex)
                        data.appendChild(createRegexElement(document));
                    if (!foundDefaultValue)
                        data.appendChild(createDefaultValueElement(document));
                }
            }
        }

        if (transformerTemplate != null)
            transformerElement.removeChild((Node) transformerTemplate);

        connector.appendChild(modeElement);

        return document;
    }
    
    private static void updateTransformerFor1_5(Document document)
    {
        Element inboundPropertiesElement, outboundPropertiesElement;
        
        NodeList transformers = document.getElementsByTagName("transformer");

        for (int i = 0; i < transformers.getLength(); i++)
        {
            Element transformerRoot = (Element) transformers.item(i);
            
            if(transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("HL7V2") && transformerRoot.getElementsByTagName("inboundProperties").getLength() == 0)
            {
                inboundPropertiesElement = document.createElement("inboundProperties");
                
                Element handleRepetitionsProperty = document.createElement("property");
                handleRepetitionsProperty.setAttribute("name", "handleRepetitions");
                handleRepetitionsProperty.setTextContent("false");
                
                Element useStrictValidationProperty = document.createElement("property");
                useStrictValidationProperty.setAttribute("name", "useStrictValidation");
                useStrictValidationProperty.setTextContent("false");
                
                Element useStrictParserProperty = document.createElement("property");
                useStrictParserProperty.setAttribute("name", "useStrictParser");
                useStrictParserProperty.setTextContent("true");
                
                inboundPropertiesElement.appendChild(handleRepetitionsProperty);
                inboundPropertiesElement.appendChild(useStrictValidationProperty);
                inboundPropertiesElement.appendChild(useStrictParserProperty);
                
                transformerRoot.appendChild(inboundPropertiesElement);
            }
            
            if(transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("HL7V2") && transformerRoot.getElementsByTagName("outboundProperties").getLength() == 0)
            {
                outboundPropertiesElement = document.createElement("outboundProperties");
                
                Element handleRepetitionsProperty = document.createElement("property");
                handleRepetitionsProperty.setAttribute("name", "handleRepetitions");
                handleRepetitionsProperty.setTextContent("false");
                
                Element useStrictValidationProperty = document.createElement("property");
                useStrictValidationProperty.setAttribute("name", "useStrictValidation");
                useStrictValidationProperty.setTextContent("false");
                
                Element useStrictParserProperty = document.createElement("property");
                useStrictParserProperty.setAttribute("name", "useStrictParser");
                useStrictParserProperty.setTextContent("true");
                
                outboundPropertiesElement.appendChild(handleRepetitionsProperty);
                outboundPropertiesElement.appendChild(useStrictValidationProperty);
                outboundPropertiesElement.appendChild(useStrictParserProperty);
                
                transformerRoot.appendChild(outboundPropertiesElement);
            }
        }
    }
    
    public static String convertTransformer(File transformer, Protocol incoming, Protocol outgoing) throws Exception
    {
        String transformerXML = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(transformer);
        Element transformerRoot = document.getDocumentElement();

        String template = "";
        Element transformerTemplate = null;
        if (transformerRoot.getElementsByTagName("template").getLength() > 0)
        {
            transformerTemplate = (Element) transformerRoot.getElementsByTagName("template").item(0);
            if (transformerTemplate != null)
                template = transformerTemplate.getTextContent();
        }

        Element inboundTemplateElement = null, outboundTemplateElement = null, inboundProtocolElement = null, outboundProtocolElement = null, inboundPropertiesElement = null, outboundPropertiesElement = null, modeElement = null;
        if (transformerRoot.getElementsByTagName("inboundTemplate").getLength() == 0)
            inboundTemplateElement = document.createElement("inboundTemplate");
        if (transformerRoot.getElementsByTagName("outboundTemplate").getLength() == 0)
            outboundTemplateElement = document.createElement("outboundTemplate");
        if (transformerRoot.getElementsByTagName("inboundProtocol").getLength() == 0)
            inboundProtocolElement = document.createElement("inboundProtocol");
        if (transformerRoot.getElementsByTagName("outboundProtocol").getLength() == 0)
            outboundProtocolElement = document.createElement("outboundProtocol");

        if (transformerTemplate != null)
        {
            if (incoming == Protocol.HL7V2 && outgoing == Protocol.HL7V2)
            {
                inboundTemplateElement.setTextContent(template);
            }
            else if (outgoing == Protocol.HL7V2)
            {
                outboundTemplateElement.setTextContent(template);
            }

            inboundProtocolElement.setTextContent(incoming.toString());
            outboundProtocolElement.setTextContent(outgoing.toString());
        }

        if (transformerRoot.getElementsByTagName("inboundTemplate").getLength() == 0)
            transformerRoot.appendChild(inboundTemplateElement);
        if (transformerRoot.getElementsByTagName("outboundTemplate").getLength() == 0)
            transformerRoot.appendChild(outboundTemplateElement);
        if (transformerRoot.getElementsByTagName("inboundProtocol").getLength() == 0)
            transformerRoot.appendChild(inboundProtocolElement);
        if (transformerRoot.getElementsByTagName("outboundProtocol").getLength() == 0)
            transformerRoot.appendChild(outboundProtocolElement);

        // replace HL7 Message builder with Message Builder
        NodeList steps = transformerRoot.getElementsByTagName("com.webreach.mirth.model.Step");

        for (int i = 0; i < steps.getLength(); i++)
        {
            Element step = (Element) steps.item(i);
            NodeList stepTypesList = step.getElementsByTagName("type");
            if (stepTypesList.getLength() > 0)
            {
                Element stepType = (Element) stepTypesList.item(0);
                if (stepType.getTextContent().equals("HL7 Message Builder"))
                {
                    stepType.setTextContent("Message Builder");
                }

                if (stepType.getTextContent().equals("Message Builder") || stepType.getTextContent().equals("Mapper"))
                {
                    boolean foundRegex = false, foundDefaultValue = false;
                    Element data = (Element) step.getElementsByTagName("data").item(0);
                    NodeList entries = data.getElementsByTagName("entry");
                    
                    for (int j = 0; j < entries.getLength(); j++)
                    {
                        NodeList strings = ((Element)entries.item(j)).getElementsByTagName("string");
                        
                        if (strings.getLength() > 0)
                        {
                            if (strings.item(0).getTextContent().equals("RegularExpressions"))
                                foundRegex = true;
                            else if (strings.item(0).getTextContent().equals("DefaultValue"))
                                foundDefaultValue = true;
                            
                            if(strings.item(0).getTextContent().equals("isGlobal"))
                            {
                                if(strings.item(1).getTextContent().equals("0"))
                                    strings.item(1).setTextContent("channel");
                                else if(strings.item(1).getTextContent().equals("1"))
                                    strings.item(1).setTextContent("global");
                            }
                        }
                    }

                    if (!foundRegex)
                        data.appendChild(createRegexElement(document));
                    if (!foundDefaultValue)
                        data.appendChild(createDefaultValueElement(document));
                }
            }
        }

        if (transformerTemplate != null)
            transformerRoot.removeChild((Node) transformerTemplate);

        DocumentSerializer docSerializer = new DocumentSerializer();
        transformerXML = docSerializer.toXML(document);

        return updateLocalAndGlobalVariables(transformerXML);
    }

    public static String convertFilter(File filter) throws Exception
    {
        String filterXML = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            document = builder.parse(filter);
            Element filterRoot = document.getDocumentElement();
            Element filterTemplate = null;

            if (filterRoot.getElementsByTagName("template").getLength() > 0)
            {
                filterTemplate = (Element) filterRoot.getElementsByTagName("template").item(0);
                if (filterTemplate != null)
                    filterRoot.removeChild(filterTemplate);
            }

            DocumentSerializer docSerializer = new DocumentSerializer();
            filterXML = docSerializer.toXML(document);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return updateLocalAndGlobalVariables(filterXML);
    }

    public static Element createRegexElement(Document document)
    {
        Element entryElement = document.createElement("entry");
        Element regexElement = document.createElement("string");
        Element stringArrayElement = document.createElement("string-array");
        Element listElement = document.createElement("list");

        regexElement.setTextContent("RegularExpressions");


        entryElement.appendChild(regexElement);
        entryElement.appendChild(listElement);

        return entryElement;
    }

    public static Element createDefaultValueElement(Document document)
    {
        Element entryElement = document.createElement("entry");
        Element defaultValueElement = document.createElement("string");
        Element defaultValueValueElement = document.createElement("string");

        defaultValueElement.setTextContent("DefaultValue");

        entryElement.appendChild(defaultValueElement);
        entryElement.appendChild(defaultValueValueElement);

        return entryElement;
    }

    public static String updateLocalAndGlobalVariables(String xml) throws Exception
    {
        xml = xml.replaceAll("localMap.put", "channelMap.put");
        xml = xml.replaceAll("localMap.get", "channelMap.get");
        return xml;
    }
}
