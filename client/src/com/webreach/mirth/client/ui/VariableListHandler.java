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

package com.webreach.mirth.client.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXList;

import com.webreach.mirth.client.ui.panels.reference.ReferenceTable;

public class VariableListHandler extends TransferHandler
{
    String prefix, suffix;

    public VariableListHandler(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    protected Transferable createTransferable(JComponent c)
    {
        try
        {
            String text = "";
            if (c instanceof JXList)
            {
                JXList list = ((JXList) (c));
                if (list == null)
                    return null;
                text = (String) list.getSelectedValue();
            }
            else if (c instanceof ReferenceTable)
            {
                ReferenceTable reftable = ((ReferenceTable) (c));
                if (reftable == null)
                    return null;

                int currRow = reftable.getSelectedRow();

                if (currRow >= 0 && currRow < reftable.getRowCount())
                    text = (String) reftable.getValueAt(currRow, 0);
            }

            if (text != null)
            {
                return new VariableTransferable(text, prefix, suffix);
            }
            return null;
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
