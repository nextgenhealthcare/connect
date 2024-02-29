/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import javax.swing.event.DocumentListener;

import com.mirth.connect.client.ui.codetemplate.CodeTemplatePanel;
import com.mirth.connect.client.ui.codetemplate.CodeTemplatePropertiesPanel;

public abstract class CodeTemplateTypePlugin extends ClientPlugin {

    public CodeTemplateTypePlugin(String pluginName) {
        super(pluginName);
    }

    public abstract CodeTemplatePropertiesPanel getPanel(CodeTemplatePanel parent, DocumentListener codeChangeListener);
}