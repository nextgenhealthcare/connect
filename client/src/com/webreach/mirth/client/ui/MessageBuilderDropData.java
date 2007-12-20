package com.webreach.mirth.client.ui;

public class MessageBuilderDropData
{
    private String messageSegment;
    private String mapping;
    
    public MessageBuilderDropData(String messageSegment, String mapping)
    {
        setMessageSegment(messageSegment);
        setMapping(mapping);
    }

    public String getMapping()
    {
        return mapping;
    }

    public void setMapping(String mapping)
    {
        this.mapping = mapping;
    }

    public String getMessageSegment()
    {
        return messageSegment;
    }

    public void setMessageSegment(String messageSegment)
    {
        this.messageSegment = messageSegment;
    }
}
