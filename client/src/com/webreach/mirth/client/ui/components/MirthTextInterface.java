package com.webreach.mirth.client.ui.components;

public interface MirthTextInterface {

    public void cut();

    public void copy();

    public void paste();

    public void selectAll();

    public void replaceSelection(String text);

    public boolean isEditable();

    public boolean isEnabled();

    public boolean isVisible();

    public String getSelectedText();

    public String getText();
}
