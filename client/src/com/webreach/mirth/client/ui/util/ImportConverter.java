package com.webreach.mirth.client.ui.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.DocumentSerializer;

public class ImportConverter
{
    private enum Direction {
        INBOUND, OUTBOUND
    }
    
    private enum Mode {
        ROUTER, BROADCAST
    }
    
    /*
     * Upgrade pre-1.4 channels to work with 1.4+
     */
    public static String convertChannel(File channel)
    {
        String channelXML = "";
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder; 
        try
        {
                builder = factory.newDocumentBuilder();
                document = builder.parse(channel);
                Element channelRoot = document.getDocumentElement();
                
                Direction direction = null;
                Mode mode = null;
                
                if(channelRoot.getElementsByTagName("direction").getLength() > 0)
                {
                    Node channelDirection = channelRoot.getElementsByTagName("direction").item(0);
                    
                    if(channelDirection.getTextContent().equals("INBOUND"))
                        direction = Direction.INBOUND;
                    else if(channelDirection.getTextContent().equals("OUTBOUND"))
                        direction = Direction.OUTBOUND;
                    
                    channelRoot.removeChild(channelDirection);
                }
                
                NodeList modeElements = channelRoot.getElementsByTagName("mode");                
                if(modeElements.getLength() > 0)
                {
                    for(int i = 0; i < modeElements.getLength(); i++)
                    {
                        if(((Element)modeElements.item(i)).getParentNode() == channelRoot)
                        {
                            Node channelMode = modeElements.item(i);
                            
                            if(channelMode.getTextContent().equals("BROADCAST"))
                                mode = Mode.BROADCAST;
                            else if(channelMode.getTextContent().equals("ROUTER"))
                                mode = Mode.ROUTER;
                            
                            channelRoot.removeChild(channelMode);
                        }
                    }
                }
                
                if(channelRoot.getElementsByTagName("protocol").getLength() > 0)
                {
                    Node channelProtocol = channelRoot.getElementsByTagName("protocol").item(0);
                    channelRoot.removeChild(channelProtocol);
                }
                
                NodeList transportNames = channelRoot.getElementsByTagName("transportName");
                for(int i = 0; i < transportNames.getLength(); i++)
                {
                    if(transportNames.item(i).getTextContent().equals("PDF Writer"))
                    {
                        transportNames.item(i).setTextContent("Document Writer");
                    }
                }
                
                NodeList properyNames = channelRoot.getElementsByTagName("property");
                for(int i = 0; i < properyNames.getLength(); i++)
                {
                    Node nameAttribute = properyNames.item(i).getAttributes().getNamedItem("name");
                    if(properyNames.item(i).getAttributes().getLength() > 0 && nameAttribute != null)
                    {
                        if(nameAttribute.getNodeValue().equals("DataType"))
                        {
                            if(properyNames.item(i).getTextContent().equals("PDF Writer"))
                            {
                                properyNames.item(i).setTextContent("Document Writer");
                            }
                        }
                    }
                }
                
                if(direction == Direction.OUTBOUND)
                {
                    Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                    document = getUpdatedTransformer(document, Direction.OUTBOUND, sourceConnectorRoot);
                    
                    Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);
                    
                    NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");
                    
                    for(int i = 0; i < destinationsConnectors.getLength(); i++)
                    {
                        document = getUpdatedTransformer(document, Direction.OUTBOUND, (Element) destinationsConnectors.item(i));
                    }
                }
                else if(direction == Direction.INBOUND) 
                {
                    if(mode == Mode.BROADCAST)
                    {
                        Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                        document = getUpdatedTransformer(document, Direction.INBOUND, sourceConnectorRoot);
                        
                        Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);
                        
                        NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");
                        
                        for(int i = 0; i < destinationsConnectors.getLength(); i++)
                        {
                            document = getUpdatedTransformer(document, Direction.INBOUND, (Element) destinationsConnectors.item(i));
                        }
                    }
                    else if(mode == Mode.ROUTER)
                    {
                        Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                        document = getUpdatedTransformer(document, Direction.INBOUND, sourceConnectorRoot);
                        
                        Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);
                        
                        NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.webreach.mirth.model.Connector");
                        
                        for(int i = 0; i < destinationsConnectors.getLength(); i++)
                        {
                            document = getUpdatedTransformer(document, Direction.INBOUND, (Element) destinationsConnectors.item(i));
                        }
                    }
                }
                
                DocumentSerializer docSerializer = new DocumentSerializer();
                channelXML = docSerializer.toXML(document);
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

        return updateLocalAndGlobalVariables(channelXML);
    }
    
    /*
     * Upgrade pre-1.4 channels' transformers and filters to work with 1.4+
     */
    private static Document getUpdatedTransformer(Document document, Direction direction, Element connector)
    {
        Element filterElement = (Element) connector.getElementsByTagName("filter").item(0);
        Element filterTemplate = null;
        
        if(filterElement.getElementsByTagName("template").getLength() > 0)
        {
            filterTemplate = (Element) filterElement.getElementsByTagName("template").item(0);
            if(filterTemplate != null)
                filterElement.removeChild(filterElement.getElementsByTagName("template").item(0));
        }
        
        Element transformerElement = (Element) connector.getElementsByTagName("transformer").item(0);
        String template = null;
        Element transformerTemplate = null;
        
        if(transformerElement.getElementsByTagName("template").getLength() > 0)
        {
            transformerTemplate = (Element) transformerElement.getElementsByTagName("template").item(0);
            if(transformerTemplate != null)
                template = transformerTemplate.getTextContent();
        }

        Element inboundTemplateElement = document.createElement("inboundTemplate");
        Element outboundTemplateElement = document.createElement("outboundTemplate");
        Element inboundProtocolElement = document.createElement("inboundProtocol");
        Element outboundProtocolElement = document.createElement("outboundProtocol");

        Element modeElement = document.createElement("mode");
        
        if(direction == Direction.OUTBOUND && connector.getTagName().equals("sourceConnector"))
        {
            inboundProtocolElement.setTextContent(MessageObject.Protocol.XML.toString());
            outboundProtocolElement.setTextContent(MessageObject.Protocol.XML.toString());
        }
        else if(direction == Direction.OUTBOUND && connector.getTagName().equals("com.webreach.mirth.model.Connector"))
        {
            inboundProtocolElement.setTextContent(MessageObject.Protocol.XML.toString());
            outboundProtocolElement.setTextContent(MessageObject.Protocol.HL7V2.toString());
            if(template != null)
                outboundTemplateElement.setTextContent(template);
        }
        else if(direction == Direction.INBOUND)
        {
            inboundProtocolElement.setTextContent(MessageObject.Protocol.HL7V2.toString());
            outboundProtocolElement.setTextContent(MessageObject.Protocol.HL7V2.toString());
            if(template != null)
                inboundTemplateElement.setTextContent(template);
        }
        
        if(connector.getTagName().equals("sourceConnector"))
            modeElement.setTextContent(Connector.Mode.SOURCE.toString());
        else
            modeElement.setTextContent(Connector.Mode.DESTINATION.toString());
        
        transformerElement.appendChild(inboundTemplateElement);
        transformerElement.appendChild(outboundTemplateElement);
        transformerElement.appendChild(inboundProtocolElement);
        transformerElement.appendChild(outboundProtocolElement);
        
        // replace HL7 Message builder with Message Builder
        NodeList steps = transformerElement.getElementsByTagName("com.webreach.mirth.model.Step");
        
        for(int i = 0; i < steps.getLength(); i++)
        {
            Element step = (Element) steps.item(i);
            NodeList stepTypesList = step.getElementsByTagName("type");
            if(stepTypesList.getLength() > 0)
            {
                Element stepType = (Element) stepTypesList.item(0);
                if(stepType.getTextContent().equals("HL7 Message Builder"))
                {
                    stepType.setTextContent("Message Builder");
                }
                
                if(stepType.getTextContent().equals("Message Builder") || stepType.getTextContent().equals("Mapper"))
                {
                    boolean foundRegex = false, foundDefaultValue = false;
                    Element data = (Element) step.getElementsByTagName("data").item(0);
                    NodeList entries = data.getElementsByTagName("entry");
                    for(int j = 0; j < entries.getLength(); j++)
                    {
                        if(((Element)entries.item(j)).getElementsByTagName("string").getLength() > 0)
                        {
                            if(((Element)entries.item(j)).getElementsByTagName("string").item(0).getTextContent().equals("RegularExpressions"))
                                foundRegex = true;
                            else if (((Element)entries.item(j)).getElementsByTagName("string").item(0).getTextContent().equals("DefaultValue"))
                                foundDefaultValue = true;
                        }
                    }
                    
                    if(!foundRegex)
                        data.appendChild(createRegexElement(document));
                    if(!foundDefaultValue)
                        data.appendChild(createDefaultValueElement(document));
                }
            } 
        }
                
        if(transformerTemplate != null)
            transformerElement.removeChild((Node)transformerTemplate);
        
        connector.appendChild(modeElement);
        
        return document;
    }
    
    public static String convertTransformer(File transformer, Protocol incoming, Protocol outgoing)
    {
        String transformerXML = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder; 
        try
        {
                builder = factory.newDocumentBuilder();
                document = builder.parse(transformer);
                Element transformerRoot = document.getDocumentElement();
                
                String template = "";
                Element transformerTemplate = null;
                if(transformerRoot.getElementsByTagName("template").getLength() > 0)
                {
                    transformerTemplate = (Element) transformerRoot.getElementsByTagName("template").item(0);
                    if(transformerTemplate != null)
                        template = transformerTemplate.getTextContent();
                }
                
                Element inboundTemplateElement = null, outboundTemplateElement = null, inboundProtocolElement = null, outboundProtocolElement = null, inboundPropertiesElement = null, outboundPropertiesElement = null, modeElement = null;
                if(transformerRoot.getElementsByTagName("inboundTemplate").getLength() == 0)
                    inboundTemplateElement = document.createElement("inboundTemplate");
                if(transformerRoot.getElementsByTagName("outboundTemplate").getLength() == 0)
                    outboundTemplateElement = document.createElement("outboundTemplate");
                if(transformerRoot.getElementsByTagName("inboundProtocol").getLength() == 0)
                    inboundProtocolElement = document.createElement("inboundProtocol");
                if(transformerRoot.getElementsByTagName("outboundProtocol").getLength() == 0)
                    outboundProtocolElement = document.createElement("outboundProtocol");
                
                if(transformerTemplate != null)
                {
                    if(incoming == Protocol.HL7V2 && outgoing == Protocol.HL7V2)              
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
                
                if(transformerRoot.getElementsByTagName("inboundTemplate").getLength() == 0)
                    transformerRoot.appendChild(inboundTemplateElement);
                if(transformerRoot.getElementsByTagName("outboundTemplate").getLength() == 0)
                    transformerRoot.appendChild(outboundTemplateElement);
                if(transformerRoot.getElementsByTagName("inboundProtocol").getLength() == 0)
                    transformerRoot.appendChild(inboundProtocolElement);
                if(transformerRoot.getElementsByTagName("outboundProtocol").getLength() == 0)
                    transformerRoot.appendChild(outboundProtocolElement);
                
                // replace HL7 Message builder with Message Builder
                NodeList steps = transformerRoot.getElementsByTagName("com.webreach.mirth.model.Step");
                
                for(int i = 0; i < steps.getLength(); i++)
                {
                    Element step = (Element) steps.item(i);
                    NodeList stepTypesList = step.getElementsByTagName("type");
                    if(stepTypesList.getLength() > 0)
                    {
                        Element stepType = (Element) stepTypesList.item(0);
                        if(stepType.getTextContent().equals("HL7 Message Builder"))
                        {
                            stepType.setTextContent("Message Builder");
                        }
                        
                        if(stepType.getTextContent().equals("Message Builder") || stepType.getTextContent().equals("Mapper"))
                        {
                            boolean foundRegex = false, foundDefaultValue = false;
                            Element data = (Element) step.getElementsByTagName("data").item(0);
                            NodeList entries = data.getElementsByTagName("entry");
                            for(int j = 0; j < entries.getLength(); j++)
                            {
                                if(((Element)entries.item(j)).getElementsByTagName("string").getLength() > 0)
                                {
                                    if(((Element)entries.item(j)).getElementsByTagName("string").item(0).getTextContent().equals("RegularExpressions"))
                                        foundRegex = true;
                                    else if (((Element)entries.item(j)).getElementsByTagName("string").item(0).getTextContent().equals("DefaultValue"))
                                        foundDefaultValue = true;
                                }
                            }
                            
                            if(!foundRegex)
                                data.appendChild(createRegexElement(document));
                            if(!foundDefaultValue)
                                data.appendChild(createDefaultValueElement(document));
                        }
                    } 
                }
                
                if(transformerTemplate != null)
                    transformerRoot.removeChild((Node)transformerTemplate);
                
                DocumentSerializer docSerializer = new DocumentSerializer();
                transformerXML = docSerializer.toXML(document);
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
        return updateLocalAndGlobalVariables(transformerXML);
    }

    public static String convertFilter(File filter)
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
                
                if(filterRoot.getElementsByTagName("template").getLength() > 0)
                {
                    filterTemplate = (Element) filterRoot.getElementsByTagName("template").item(0);
                    if(filterTemplate != null)
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
        Element treeElement = document.createElement("tree-map");
        Element comparatorElement = document.createElement("no-comparator");
        
        regexElement.setTextContent("RegularExpressions");
        
        treeElement.appendChild(comparatorElement);
        
        entryElement.appendChild(regexElement);
        entryElement.appendChild(treeElement);
        
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
    
    public static String updateLocalAndGlobalVariables(String xml)
    {
        xml = xml.replaceAll("localMap.put", "channelContextMap.put");
        xml = xml.replaceAll("localMap.get", "channelContextMap.get");
        xml = xml.replaceAll("globalMap.put", "globalContextMap.put");
        xml = xml.replaceAll("globalMap.put", "globalContextMap.put");
        return xml;
    }
}
