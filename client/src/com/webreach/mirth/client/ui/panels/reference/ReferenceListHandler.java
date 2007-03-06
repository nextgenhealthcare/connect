/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.panels.reference;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.webreach.mirth.client.ui.VariableTransferable;

public class ReferenceListHandler extends TransferHandler
{
    private ArrayList<ReferenceListItem> listItems;

    public ReferenceListHandler(ArrayList<ReferenceListItem> listItems)
    {
        super();
        this.listItems = listItems;
    }

    public void setListItems(ArrayList<ReferenceListItem> listItems)
    {
        this.listItems = listItems;
    }

    protected Transferable createTransferable(JComponent c)
    {
        try
        {
            if (listItems == null)
                return null;
            ReferenceTable reftable = ((ReferenceTable) (c));

            if (reftable == null)
                return null;

            int currRow = reftable.getSelectedRow();

            String text;
            if (currRow >= 0 && currRow < reftable.getRowCount() && currRow < listItems.size())
                text = listItems.get(currRow).getCode();
            else
                text = "";

            return new VariableTransferable(text, "", "");
        }
        catch (ClassCastException cce)
        {
            return null;
        }
    }

    public int getSourceActions(JComponent c)
    {
        return COPY;
    }

    public boolean canImport(JComponent c, DataFlavor[] df)
    {
        return false;
    }
}
