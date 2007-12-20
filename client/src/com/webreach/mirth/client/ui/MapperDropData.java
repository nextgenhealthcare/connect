package com.webreach.mirth.client.ui;

public class MapperDropData
{
    private String variable;
    private String mapping;
    
    public MapperDropData(String variable, String mapping)
    {
        setVariable(variable);
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

    public String getVariable()
    {
        return variable;
    }

    public void setVariable(String variable)
    {
        this.variable = variable;
    }
}
