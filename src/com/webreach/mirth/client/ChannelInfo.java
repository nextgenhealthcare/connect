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
    
    public String channelName;
    public String status;
    public String direction;
    
    public ChannelInfo()
    {
        channelName = "";
        status = "Disabled";
        direction = "Inbound";
    }
}
