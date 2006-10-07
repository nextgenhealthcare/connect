package org.mule.providers.pdf;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public class BBCodeParser
{
	private LinkedBlockingQueue<BBCodeToken> elements;

	private Stack<KeywordType> knownTags;

	private int size;

	private String color;
	
	/*
	 * Contains the tags for the parser to look for
	 */
	public enum KeywordType
	{
		ITALIC("[i]", "[/i]"), 
		UNDERLINE("[u]", "[/u]"), 
		BOLD("[b]", "[/b]"), 
		IMAGE("[img]", "[/img]"), 
		PRE("[pre]", "[/pre]"), 
		SIZE("[size=\"", "[/size]"), 
		COLOR("[color=\"", "[/color]"), 
		NEWPAGE("[n]", ""), 
		STRIKETHROUGH("[s]", "[/s]");

		public String start;

		public String end;

		KeywordType(String start, String end)
		{
			this.start = start;
			this.end = end;
		}
	}
	
	/*
	 * Initialize a new instance of the parser with a given string and parse it.
	 */
	public BBCodeParser(String BBCode)
	{
		size = 12;
		color = "black";
		elements = new LinkedBlockingQueue<BBCodeToken>();
		knownTags = new Stack<KeywordType>();
		parse(BBCode);
	}
	
	/*
	 * Recursively parses the string, and adds the elements to the list.
	 */
	private void parse(String bbCode)
	{
		String result = new String();
		int currentIndex = 0;

		while (currentIndex < bbCode.length())
		{
			if (bbCode.charAt(currentIndex) == '[')
			{
				boolean found = false;

				for (KeywordType kt : KeywordType.values())
				{
					if ((currentIndex + kt.start.length()) <= bbCode.length() && bbCode.substring(currentIndex, currentIndex + kt.start.length()).equalsIgnoreCase(kt.start))
					{
						String caseInsensitiveBBCode = bbCode.toLowerCase();
						
						if (result.length() > 0)
							createBBCodeToken(result);

						result = new String();

						if (kt == KeywordType.COLOR)
						{
							String prevColor = color;
							currentIndex += kt.start.length();

							int tagEnd = caseInsensitiveBBCode.indexOf("\"", currentIndex);
							if(tagEnd == -1)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								return;
							}
							
							color = caseInsensitiveBBCode.substring(currentIndex, tagEnd);
							currentIndex = tagEnd + 1;
							if (caseInsensitiveBBCode.charAt(currentIndex) != ']')
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								return;
							} 
							else
								currentIndex++;
	
							int endIndex = caseInsensitiveBBCode.indexOf(kt.end, currentIndex);
							if (endIndex == -1)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								return;
							} 
							else
							{
								parse(bbCode.substring(currentIndex, endIndex));
								color = prevColor;
								currentIndex = endIndex + kt.end.length();
							}
						} 
						else if (kt == KeywordType.SIZE)
						{
							int prevSize = size;
							currentIndex += kt.start.length();

							try
							{
								int tagEnd = caseInsensitiveBBCode.indexOf("\"", currentIndex);
								if(tagEnd == -1)
								{
									createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
									return;
								}
								
								size = Integer.parseInt(caseInsensitiveBBCode.substring(currentIndex, tagEnd));
								currentIndex = tagEnd + 1;
								
								if (caseInsensitiveBBCode.charAt(currentIndex) != ']')
								{
									createBBCodeToken(bbCode);
									return;
								} 
								else
									currentIndex++;
							} 
							catch (NumberFormatException e)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								return;
							}
							

							int endIndex = caseInsensitiveBBCode.indexOf(kt.end, currentIndex);
							if (endIndex == -1)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								return;
							} 
							else
							{
								parse(bbCode.substring(currentIndex, endIndex));
								size = prevSize;
								currentIndex = endIndex + kt.end.length();
							}
						} 
						else if (kt == KeywordType.PRE)
						{
							knownTags.push(kt);
							int endIndex = caseInsensitiveBBCode.indexOf(kt.end, currentIndex);
							if (endIndex == -1)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								knownTags.pop();
								return;
							} 
							else
							{
								createBBCodeToken(bbCode.substring(currentIndex + kt.start.length(), endIndex));
								knownTags.pop();
								currentIndex = endIndex + kt.end.length();
							}
						} 
						else if (kt == KeywordType.NEWPAGE)
						{
							knownTags.push(kt);
							createBBCodeToken("");
							knownTags.pop();
							currentIndex += kt.start.length();
						} 
						else
						{
							knownTags.push(kt);
							currentIndex += kt.start.length();
							int endIndex = caseInsensitiveBBCode.indexOf(kt.end, currentIndex);
							if (endIndex == -1)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								knownTags.pop();
								return;
							}

							for(int j = 0; j < countInsideTags(kt, bbCode.substring(currentIndex,bbCode.length())); j++)
							{
								endIndex = caseInsensitiveBBCode.indexOf(kt.end, endIndex + kt.end.length());
							}
							if (endIndex == -1)
							{
								createBBCodeToken(bbCode.substring(currentIndex,bbCode.length()));
								knownTags.pop();
								return;
							}
							
							parse(bbCode.substring(currentIndex, endIndex));
							knownTags.pop();
							currentIndex = endIndex + kt.end.length();
						}

						found = true;
						break;
					}
				}

				if (!found)
				{
					result += bbCode.charAt(currentIndex);
					currentIndex++;
				}
			}

			else if (currentIndex < bbCode.length())
			{
				result += bbCode.charAt(currentIndex);
				currentIndex++;
			}
		}

		if (result.length() > 0)
			createBBCodeToken(result);
	}

	/*
	 * Creates the token to be put into the array.
	 */
	private void createBBCodeToken(String value)
	{
		ArrayList<KeywordType> tags = new ArrayList<KeywordType>();

		for (int i = 0; i < knownTags.size(); i++)
		{
			if(!tags.contains(knownTags.get(i)))
				tags.add(knownTags.get(i));
		}

		elements.add(new BBCodeToken(value, tags, size, color));
	}
	
	/* 
	 * Look for the same tags inside of the tag to make sure that you do not get the wrong closing tag.
	 */
	private int countInsideTags(KeywordType kt, String bbCode)
	{
		String caseInsensitiveBBCode = bbCode.toLowerCase();
		if(caseInsensitiveBBCode.indexOf(kt.start) != -1 && caseInsensitiveBBCode.indexOf(kt.end) != -1 && caseInsensitiveBBCode.indexOf(kt.start) < caseInsensitiveBBCode.indexOf(kt.end))
			return 1 + countInsideTags(kt, bbCode.substring(caseInsensitiveBBCode.indexOf(kt.start)+kt.start.length(), caseInsensitiveBBCode.indexOf(kt.end)));
		else
			return 0;
			
	}
	
	/*
	 * Get the next token.
	 */
	public BBCodeToken getNext()
	{
		return elements.poll();
	}

	/*
	 * See if there are more tokens.
	 */
	public boolean hasNext()
	{
		return !elements.isEmpty();
	}

	/*
	 * Example for testing
	 */
	public static void main(String[] args)
	{
		BBCodeParser p;
		
		p = new BBCodeParser("Hello[img]f[/img][/b][b][pre][b][i]there[/i][/b][/pre][/b], [color=\"red\"][b]how[/b][/color][n][size=\"4\"]d[/size] are [i]you[/i]?");
		while (p.hasNext())
		{
			BBCodeToken t = p.getNext();
			System.out.println(t.getValue() + " " + t.getApplicableTags() + " size = " + t.getSize() + " color = " + t.getColor());
		}

		p = new BBCodeParser("[/i][/B]Hello [/i][u][i][s][B][size=\"67\"]there[n][/size][/b][/s][/i][/u][b][/b], how are [I]you[/i]?");
		while (p.hasNext())
		{
			BBCodeToken t = p.getNext();
			System.out.println(t.getValue() + " " + t.getApplicableTags() + " size = " + t.getSize() + " color = " + t.getColor());
		}
		
		p = new BBCodeParser("[size=\"5][pre][b][b]hey[/pre][/B][/B][/size] [u][u]hey[/u] hey[/u]");
		while (p.hasNext())
		{
			BBCodeToken t = p.getNext();
			System.out.println(t.getValue() + " " + t.getApplicableTags() + " size = " + t.getSize() + " color = " + t.getColor());
		}
	}
}
