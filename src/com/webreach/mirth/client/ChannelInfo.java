/*
 * ChannelInfo.java
 *
 * Created on May 3, 2006, 5:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client;

/**
 *
 * @author jacobb
 */
public class ChannelInfo 
{
    
    private String channelName = "";
    
    /** Creates a new instance of ChannelInfo */
    public ChannelInfo()
    {
    }
    
    public void setChannelName(String name)
    {
        channelName = name;
    }
    
    public String getChannelName()
    {
        return channelName;
    }    
}
