package com.webreach.mirth.plugins;

import java.util.ArrayList;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.model.CodeTemplate;

public class ClientPlugin
{
    protected String name;
    protected Frame parent = PlatformUI.MIRTH_FRAME;
    
    public ClientPlugin ()
    {

    }
    
    public ClientPlugin (String name)
    {
        this.name = name;
    }
    
    public ArrayList<CodeTemplate> getReferenceItems()
    {
        return new ArrayList<CodeTemplate>();
    }
    
    public String getName()
    {
        return name;
    }
}
