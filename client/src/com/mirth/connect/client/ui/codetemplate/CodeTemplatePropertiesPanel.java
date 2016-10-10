/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import java.awt.Component;
import java.util.List;

import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.model.codetemplates.CodeTemplateProperties;

public abstract class CodeTemplatePropertiesPanel {

    protected CodeTemplatePanel parent;
    protected DocumentListener codeChangeListener;

    public CodeTemplatePropertiesPanel(CodeTemplatePanel parent, DocumentListener codeChangeListener) {
        this.parent = parent;
        this.codeChangeListener = codeChangeListener;
    }

    public abstract List<Pair<Pair<Component, String>, Pair<Component, String>>> getRows();

    public abstract CodeTemplateProperties getProperties();

    public abstract void setProperties(CodeTemplateProperties properties);

    public abstract void setVisible(boolean visible);
}