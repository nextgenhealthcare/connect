/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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
