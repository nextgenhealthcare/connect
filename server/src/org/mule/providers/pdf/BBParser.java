/*
 * BBParser.java
 *
 * Created on October 4, 2006, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.util;

/**
 *
 * @author brendanh
 */
public class BBParser
{
    private String currentString = ""; 
    private String[] startKeywords = {"", "[i]","[u]","[b]"};
    private String[] endKeywords = {"","[/i]","[/u]","[/b]"};
    private String[] keywordType = {"NORMAL","ITALIC", "UNDERLINE", "BOLD"};
    private int currentIndex;

    /**
     * Creates a new instance of BBParser
     */
    public BBParser(String current)
    {
        currentIndex = 0;
        currentString = current;
    }
    
    /*
     * Get the next token.
     */    
    public BBToken getNext()
    {
        String result = "";
        int type = -1;
        
        while(hasNext())
        {
            if(currentString.charAt(currentIndex) == '[')
            {
                for (int i = 1; i < startKeywords.length; i++ )
                {
                    if(currentString.substring(currentIndex, currentIndex + startKeywords[i].length()).equalsIgnoreCase(startKeywords[i]))
                    {
                        if(type == 0)
                            return new BBToken(result,keywordType[type]);
                        else
                        {
                            type = i;
                            currentIndex += startKeywords[type].length();
                        }
                    }
                }
                
                if(type > 0)
                {
                    for (int i = 1; i < endKeywords.length; i++ )
                    {
                        if(currentString.substring(currentIndex, currentIndex + endKeywords[i].length()).equalsIgnoreCase(endKeywords[i]))
                        {
                            if(type == i)
                            {
                                currentIndex += endKeywords[i].length();
                                return new BBToken(result,keywordType[type]);
                            }
                        }
                    }
                }
            }
            else if(type == -1 )
                type = 0;

            result += currentString.charAt(currentIndex);
            currentIndex++;   
        }
        
        return new BBToken(result,keywordType[type]);
    }
    
    /*
     *  See if there are more tokens.
     */
    public boolean hasNext()
    {
        return currentIndex < (currentString.length());
    }
    
    
    /*
     * Example for testing
     */
    public static void main(String [] args)
    {
        BBParser p = new BBParser("Hello [b]there[/b], how are [i]you[/i]?");
        while(p.hasNext())
        {
            BBToken t = p.getNext();
            System.out.println(t.getValue() + " " + t.getType());
        }
        
        p = new BBParser("[/i][/B]Hello [/i][B]there[/b][b][/b], how are [I]you[/i]?");
        while(p.hasNext())
        {
            BBToken t = p.getNext();
            System.out.println(t.getValue() + " " + t.getType());
        }
    }
}
