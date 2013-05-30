package com.mirth.connect.donkey.util.migration;

import java.util.List;

import org.w3c.dom.Element;

public interface DonkeyElement extends Element {
    public DonkeyElement getChildElement(String name);
    
    public List<DonkeyElement> getChildElements();

    public DonkeyElement addChildElement(String name);
    
    public DonkeyElement removeChild(String name);

    public void removeChildren();
    
    public boolean removeChildren(String name);
    
    public void setNodeName(String name);
}
