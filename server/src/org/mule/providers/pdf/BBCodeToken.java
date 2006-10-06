package org.mule.providers.pdf;

import java.util.ArrayList;
import java.util.List;

import org.mule.providers.pdf.BBCodeParser.KeywordType;

public class BBCodeToken
{
	private String value;

	private ArrayList<KeywordType> tags;

	private int size;

	private String color;

	public BBCodeToken(String value, ArrayList<KeywordType> tags, int size, String color)
	{
		this.value = value;
		this.tags = tags;
		this.size = size;
		this.color = color;
	}

	public String getValue()
	{
		return value;
	}

	public List<KeywordType> getApplicableTags()
	{
		return tags;
	}

	public int getSize()
	{
		return size;
	}

	public String getColor()
	{
		return color;
	}
}
