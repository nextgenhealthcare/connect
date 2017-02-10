/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;

public class TransformerTreeTableNode extends FilterTransformerTreeTableNode<Transformer, Step> {

    public TransformerTreeTableNode(BaseEditorPane<Transformer, Step> editorPane, Step element) {
        super(editorPane, element);
    }
}