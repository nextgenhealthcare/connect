package com.webreach.mirth.client.ui.panels.reference;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.webreach.mirth.client.ui.VariableTransferable;
import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.model.CodeTemplate.CodeSnippetType;

public class ReferenceListHandler extends TransferHandler {

    private ArrayList<CodeTemplate> listItems;
    //private final static String FUNCTION_PATTERN = "(function\\s*\\(.*\\))";

    public ReferenceListHandler(ArrayList<CodeTemplate> listItems) {
        super();
        this.listItems = listItems;
    }

    public void setListItems(ArrayList<CodeTemplate> listItems) {
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
            if (currRow >= 0 && currRow < reftable.getRowCount() && currRow < listItems.size()) {
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

            return new VariableTransferable(text, "", "");
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
