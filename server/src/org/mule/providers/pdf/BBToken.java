/*
 * BBToken.java
 *
 * Created on October 4, 2006, 11:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.util;

/**
 *
 * @author brendanh
 */
public class BBToken
{
    private String value;
    private String type;
    /**
     * Creates a new instance of BBToken
     */
    public BBToken(String value, String type)
    {
        this.value = value;
        this.type = type;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public String getType()
    {
        return type;
    }
}
