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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXList;

import com.webreach.mirth.client.ui.panels.reference.ReferenceTable;

public class VariableListHandler extends TransferHandler
{
    private String prefix, suffix;
    private Map<String, String> staticJsReferences;
    private Map<String, String> staticVelocityReferences;
    
    public VariableListHandler(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
        
        staticVelocityReferences = new HashMap<String, String> ();
        staticVelocityReferences.put("Raw Data", "${message.rawData}");
        staticVelocityReferences.put("Transformed Data", "${message.transformedData}");
        staticVelocityReferences.put("Message Type", "${message.type}");
        staticVelocityReferences.put("Message Version", "${message.version}");
        staticVelocityReferences.put("Message Source", "${message.source}");
        staticVelocityReferences.put("Message ID", "${message.id}");
        staticVelocityReferences.put("Encoded Data", "${message.encodedData}");
        staticVelocityReferences.put("Timestamp", "${SYSTIME}");
        staticVelocityReferences.put("Unique ID", "${UUID}");
        staticVelocityReferences.put("Date", "${DATE}");
        staticVelocityReferences.put("Original File Name", "${ORIGINALNAME}");
        staticVelocityReferences.put("Count", "${COUNT}");
        staticVelocityReferences.put("DICOM Message Raw Data", "${DICOMMESSAGE}");
        staticVelocityReferences.put("Formatted Date", "${date.get('yyyy-M-d H.m.s')}");
        staticVelocityReferences.put("Entity Encoder", "${encoder.encode()}");
        
        staticJsReferences = new HashMap<String, String> ();
        staticJsReferences.put("Raw Data", "messageObject.getRawData()");
        staticJsReferences.put("Transformed Data", "messageObject.getTransformedData()");
        staticJsReferences.put("Message Type", "messageObject.getType()");
        staticJsReferences.put("Message Version", "messageObject.getVersion()");
        staticJsReferences.put("Message Source", "messageObject.getSource()");
        staticJsReferences.put("Message ID", "messageObject.getId()");
        staticJsReferences.put("Encoded Data", "messageObject.getEncodedData()");
        staticJsReferences.put("Timestamp", "var dateString = DateUtil.getCurrentDate('yyyyMMddHHmmss');");
        staticJsReferences.put("Unique ID", "var uuid = UUIDGenerator.getUUID();");
        staticJsReferences.put("Date", "var date = DateUtil.getDate('pattern','date');");
        staticJsReferences.put("Count", "var count = 0;\nif(globalMap.get('count') != undefined) {\n\tcount = globalMap.get('count');\n\tcount++;\n\tglobalMap.put('count', count);\n} else {\n\tcount=1;\n\tglobalMap.put('count',count);\n}\n");
        staticJsReferences.put("Original File Name", "$('originalFilename')");
        staticJsReferences.put("DICOM Message Raw Data", "var rawData = DICOMUtil.getDICOMRawData(messageObject);");
        staticJsReferences.put("Formatted Date", "var dateString = DateUtil.getCurrentDate('yyyy-M-d H.m.s');");
        staticJsReferences.put("Entity Encoder", "var encodedMessage = Entities.getInstance().encode('message');");
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
            	if(prefix.equals("${") && suffix.equals("}")) { 
            		if(staticVelocityReferences.get(text) != null) { 
            			return new VariableTransferable(staticVelocityReferences.get(text), "", "");
            		}
            	} else { 
            		if(staticJsReferences.get(text) != null) { 
            			return new VariableTransferable(staticJsReferences.get(text), "", "");
            		}
            	}
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
