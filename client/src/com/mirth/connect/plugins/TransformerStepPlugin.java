/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import javax.swing.SwingUtilities;

import com.mirth.connect.client.ui.editors.transformer.TransformerPane;

public abstract class TransformerStepPlugin extends MirthEditorPanePlugin {

    public TransformerStepPlugin(String name) {
        super(name);
    }

    public void alertWarning(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                parent.alertWarning(parent, msg);
            }
        });
    }

    public abstract void initialize(TransformerPane pane);
}
