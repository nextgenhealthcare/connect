/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.reference;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.VariableTransferable;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;
import com.mirth.connect.util.CodeTemplateUtil;

public class ReferenceListHandler extends TransferHandler {

    private List<CodeTemplate> listItems;

    public ReferenceListHandler(List<CodeTemplate> listItems) {
        super();
        this.listItems = listItems;
    }

    public void setListItems(List<CodeTemplate> listItems) {
        this.listItems = listItems;
    }

    protected Transferable createTransferable(JComponent c) {
        try {
            if (listItems == null) {
                return null;
            }
            ReferenceTable reftable = ((ReferenceTable) (c));

            if (reftable == null) {
                return null;
            }

            int currRow = reftable.convertRowIndexToModel(reftable.getSelectedRow());

            String text = "";

            if (currRow >= 0 && currRow < reftable.getModel().getRowCount() && currRow < listItems.size()) {
                CodeTemplate template = listItems.get(currRow);
                if (template.getType() == CodeTemplateType.FUNCTION) {
                    text = template.getFunctionDefinition().getTransferData();
                } else if (template.getType() == CodeTemplateType.DRAG_AND_DROP_CODE) {
                    text = CodeTemplateUtil.stripDocumentation(template.getCode());
                }
            }

            return new VariableTransferable(text, TransferMode.RAW);
        } catch (ClassCastException cce) {
            return null;
        }
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public boolean canImport(JComponent c, DataFlavor[] df) {
        return false;
    }
}
