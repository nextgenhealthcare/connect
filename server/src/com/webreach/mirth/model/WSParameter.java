package com.webreach.mirth.model;

public class WSParameter
{
	private String name;
	private String type;
	private String value;
	
	public WSParameter()
	{
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public void setType(String type)
	{
		this.type = type;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getValue()
	{
		return value;
	}
}