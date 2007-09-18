package com.webreach.mirth.plugins;

import java.util.Map;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;

public abstract class TransformerStepPlugin extends MirthEditorPanePlugin
{
    public TransformerStepPlugin(String name, TransformerPane parent)
    {
        super(name,parent);
    }
}
