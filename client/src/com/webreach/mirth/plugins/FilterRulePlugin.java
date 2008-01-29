package com.webreach.mirth.plugins;

import com.webreach.mirth.client.ui.editors.filter.FilterPane;

public abstract class FilterRulePlugin extends MirthEditorPanePlugin
{
    public FilterRulePlugin(String name, FilterPane parent)
    {
        super(name,parent);
    }
}
