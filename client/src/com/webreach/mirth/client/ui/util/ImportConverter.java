package com.webreach.mirth.client.ui.util;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.model.Rule;
import java.util.Iterator;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Channel.Direction;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import java.util.Map;

public class ImportConverter
{
    public Channel convertChannel(Channel channel)
    {
        Connector connector;
        
        // source connector
        connector = channel.getSourceConnector();
        if(connector.getProperties().getProperty("DataType").equals("LLP Listener") && 
                connector.getProperties().getProperty("sendACK").equals(UIConstants.YES_OPTION) && 
                channel.getProperties().get("synchronous").equals("false"))
        {
            channel.getProperties().setProperty("synchronous","true");
        }
            
        if (connector.getFilter() != null && connector.getFilter().getRules() != null)
        {
            for (Iterator iterator = connector.getFilter().getRules().iterator(); iterator.hasNext();)
            {
                Rule rule = (Rule) iterator.next();
                String script = rule.getScript();

                if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
                    script = script.replaceAll("hl7_xml", "tmp");
                else
                    script = script.replaceAll("hl7_xml", "msg");

                script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");
                rule.setScript(script);
            }            
        } 
        if (connector.getProperties().getProperty("template") != null)
        {
            String template = connector.getProperties().getProperty("template");
            template = template.replaceAll("\\$\\{HL7 XML\\}", "\\$\\{message.transformedData\\}");
            
            if(template.indexOf("${DATE}") != -1)
                    template = template.replaceAll("\\$\\{DATE\\}", "\\$\\{date\\}");;

            if (channel.getDirection().equals(Channel.Direction.INBOUND))
            {
                template = template.replaceAll("\\$\\{HL7 ER7\\}", "\\$\\{message.rawData\\}");
            }
            else
            {
                template = template.replaceAll("\\$\\{HL7 ER7\\}", "\\$\\{message.encodedData\\}");
            }

            connector.getProperties().setProperty("template", template);
        }
        if (connector.getTransformer() != null && connector.getTransformer().getSteps() != null)
        {
            for (Iterator iterator = connector.getTransformer().getSteps().iterator(); iterator.hasNext();)
            {
                Step step = (Step) iterator.next();
                Map data = (Map) step.getData();
                if(step.getType().equals(TransformerPane.JAVASCRIPT_TYPE))
                {
                    String script = (String)data.get("Script");
                    if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
                        script = script.replaceAll("hl7_xml", "tmp");
                    else
                        script = script.replaceAll("hl7_xml", "msg");

                    script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                    script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");
                    script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                    script = script.replaceAll("incomingMessage", "messageObject.getRawData()");
                    data.put("Script", script);
                    step.setData(data);
                }
                else if(step.getType().equals(TransformerPane.MAPPER_TYPE) || step.getType().equals(TransformerPane.HL7MESSAGE_TYPE))
                {
                    String script = (String)data.get("Mapping");
                    String variable = (String)data.get("Variable");

                    if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
                    {
                        script = script.replaceAll("hl7_xml", "tmp");
                        variable = variable.replaceAll("hl7_xml", "tmp");
                    }
                    else
                    {
                        script = script.replaceAll("hl7_xml", "msg");
                        variable = variable.replaceAll("hl7_xml", "msg");
                    }

                    script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                    script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");
                    script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                    script = script.replaceAll("incomingMessage", "messageObject.getRawData()");
                    
                    data.put("Mapping", script);
                    data.put("Variable", variable);
                    step.setData(data);
                }
            }
        }
        
        // destination
        for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();)
        {
            connector = (Connector) iter.next();
            if (connector.getFilter() != null && connector.getFilter().getRules() != null)
            {
                for (Iterator iterator = connector.getFilter().getRules().iterator(); iterator.hasNext();)
                {
                    Rule rule = (Rule) iterator.next();
                    String script = rule.getScript();

                    if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
                    {
                        script = script.replaceAll("hl7_xml", "tmp");
                    }
                    else
                    {
                        script = script.replaceAll("hl7_xml", "msg");
                    }
                    script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                    script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");
                    script = script.replaceAll("incomingMessage", "messageObject.getRawData()");
                    script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                    rule.setScript(script);
                }
            }
            if (connector.getProperties().getProperty("template") != null)
            {
                String template = connector.getProperties().getProperty("template");
                template = template.replaceAll("\\$\\{HL7 XML\\}", "\\$\\{message.transformedData\\}");
                
                if(template.indexOf("${DATE}") != -1)
                    template = template.replaceAll("\\$\\{DATE\\}", "\\$\\{date\\}");
                
                if (channel.getDirection().equals(Channel.Direction.INBOUND))
                {
                    template = template.replaceAll("\\$\\{HL7 ER7\\}", "\\$\\{message.rawData\\}");
                }
                else
                {
                    template = template.replaceAll("\\$\\{HL7 ER7\\}", "\\$\\{message.encodedData\\}");
                }
                
                connector.getProperties().setProperty("template", template);
            }
            
            if (connector.getTransformer() != null && connector.getTransformer().getSteps() != null)
            {
     	       for (Iterator iterator = connector.getTransformer().getSteps().iterator(); iterator.hasNext();)
               {
                    Step step = (Step) iterator.next();
                    Map data = (Map) step.getData();
                    if(step.getType().equals(TransformerPane.JAVASCRIPT_TYPE))
                    {
                        String script = (String)data.get("Script");
                        if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
                        {
                            script = script.replaceAll("hl7_xml", "tmp");
                        }
                        else
                        {
                            script = script.replaceAll("hl7_xml", "msg");
                        }

                        script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                        script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                        script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");

                        data.put("Script", script);
                        step.setData(data);
                    }
                    else if(step.getType().equals(TransformerPane.MAPPER_TYPE) || step.getType().equals(TransformerPane.HL7MESSAGE_TYPE))
                    {
                        String script = (String)data.get("Mapping");
                        String variable = (String)data.get("Variable");

                        if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
                        {
                            script = script.replaceAll("hl7_xml", "tmp");
                            variable = variable.replaceAll("hl7_xml", "tmp");
                        }
                        else
                        {
                            script = script.replaceAll("hl7_xml", "msg");
                            variable = variable.replaceAll("hl7_xml", "msg");
                        }

                        script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                        script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                        script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");

                        data.put("Mapping", script);
                        data.put("Variable", variable);
                        step.setData(data);
                    }
                }
            }
        }

        return channel;
    }
    
    public Transformer convertTransformer(Transformer transformer, Direction direction)
    {
        for (Iterator iterator = transformer.getSteps().iterator(); iterator.hasNext();)
        {
            Step step = (Step) iterator.next();
            Map data = (Map) step.getData();
            if(step.getType().equals(TransformerPane.JAVASCRIPT_TYPE))
            {
                String script = (String)data.get("Script");
                
                if (direction.equals(Channel.Direction.OUTBOUND))
                {
                    script = script.replaceAll("hl7_xml", "tmp");
                }
                else
                {
                    script = script.replaceAll("hl7_xml", "msg");
                }

                script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");

                data.put("Script", script);
                step.setData(data);
            }
            else if(step.getType().equals(TransformerPane.MAPPER_TYPE) || step.getType().equals(TransformerPane.HL7MESSAGE_TYPE))
            {
                String script = (String)data.get("Mapping");
                String variable = (String)data.get("Variable");
                
                if (direction.equals(Channel.Direction.OUTBOUND))
                {
                    script = script.replaceAll("hl7_xml", "tmp");
                    variable = variable.replaceAll("hl7_xml", "tmp");
                }
                else
                {
                    script = script.replaceAll("hl7_xml", "msg");
                    variable = variable.replaceAll("hl7_xml", "msg");
                }

                script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
                script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
                script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");

                data.put("Mapping", script);
                data.put("Variable", variable);
                step.setData(data);
            }
        }
        
        return transformer;
    }
    
    public Filter convertFilter(Filter filter, Direction direction)
    {
        for (Iterator iterator = filter.getRules().iterator(); iterator.hasNext();)
        {
            Rule rule = (Rule) iterator.next();
            String script = rule.getScript();
            
            if (direction.equals(Channel.Direction.OUTBOUND))
            {
                script = script.replaceAll("hl7_xml", "tmp");
            }
            else
            {
                script = script.replaceAll("hl7_xml", "msg");
            }
            
            script = script.replaceAll("text\\(\\)\\[0\\]", "toString\\(\\)");
            script = script.replaceAll("er7util.ConvertToER7", "serializer.fromXML");
            script = script.replaceAll("er7util.ConvertToXML", "serializer.toXML");
            
            rule.setScript(script);
        }
        
        return filter;
    }
}
