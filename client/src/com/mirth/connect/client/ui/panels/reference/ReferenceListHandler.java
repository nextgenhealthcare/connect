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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.VariableTransferable;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;

public class ReferenceListHandler extends TransferHandler {

    private List<CodeTemplate> listItems;

    //private final static String FUNCTION_PATTERN = "(function\\s*\\(.*\\))";

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

            String text;
            if (currRow >= 0 && currRow < reftable.getModel().getRowCount() && currRow < listItems.size()) {
                CodeTemplate template = listItems.get(currRow);
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    String FUNCTION_PATTERN = "function\\s*(.*\\(.*\\))";
                    Pattern pattern = Pattern.compile(FUNCTION_PATTERN);
                    Matcher matcher = pattern.matcher(template.getCode());
                    if (matcher.find()) {
                        text = matcher.group(1);
                    } else {
                        text = "Bad Function Definition!";
                    }
                } else {
                    text = template.getCode();
                }
            } else {
                text = "";
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
