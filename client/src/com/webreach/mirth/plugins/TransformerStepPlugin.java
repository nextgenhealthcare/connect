package com.webreach.mirth.plugins;

import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;

public abstract class TransformerStepPlugin extends MirthEditorPanePlugin {

    public TransformerStepPlugin(String name) {
        super(name);
    }

    public TransformerStepPlugin(String name, TransformerPane parent) {
        super(name, parent);
    }
}
