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

/**
 * Package Database Variables for movement.
 */
public class VariableTransferable implements Transferable
{

    private static DataFlavor[] flavors = null;
    private String data = null;
    private String _prefix = "msg['";
    private String _suffix = "']";

    /**
     * @param data
     *            the type of Ant element being transferred, e.g., target, task,
     *            type, etc.
     */
    public VariableTransferable(String data, String prefix, String suffix)
    {
        if (data.equals("Any"))
            this.data = "ERROR";
        else if (data.equals("Server"))
            this.data = "ERROR-000";
        else if (data.equals("Client"))
            this.data = "ERROR-100";
        else if (data.equals("200: Filter"))
            this.data = "ERROR-200";
        else if (data.equals("300: Transformer"))
            this.data = "ERROR-300";
        else if (data.equals("301: Transformer conversion"))
            this.data = "ERROR-301";
        else if (data.equals("302: Custom transformer"))
            this.data = "ERROR-302";
        else if (data.equals("400: Connector"))
            this.data = "ERROR-400";
        else if (data.equals("401: Document connector"))
            this.data = "ERROR-401";
        else if (data.equals("402: SMTP connector"))
            this.data = "ERROR-402";
        else if (data.equals("403: File connector"))
            this.data = "ERROR-403";
        else if (data.equals("404: HTTP connector"))
            this.data = "ERROR-404";
        else if (data.equals("405: FTP connector"))
            this.data = "ERROR-405";
        else if (data.equals("406: JDBC Connector"))
            this.data = "ERROR-406";
        else if (data.equals("407: JMS Connector"))
            this.data = "ERROR-407";
        else if (data.equals("408: MLLP Connector"))
            this.data = "ERROR-408";
        else if (data.equals("409: SFTP Connector"))
            this.data = "ERROR-409";
        else if (data.equals("410: SOAP Connector"))
            this.data = "ERROR-410";
        else if (data.equals("411: TCP Connector"))
            this.data = "ERROR-411";
        else if (data.equals("412: VM Connector"))
            this.data = "ERROR-412";
        else if (data.equals("413: Email Connector"))
            this.data = "ERROR-413";        
        else if (data.equals("CDATA Tag")){
            this.data = "<![CDATA[]]>";
            prefix = "";
            suffix = "";
        }
        else
            this.data = data;
        _prefix = prefix;
        _suffix = suffix;
        init();
    }

    /**
     * Set up the supported flavors: DataFlavor.stringFlavor for a raw string
     * containing an Ant element name (e.g. task, target, etc), or an
     * ElementFlavor containing an ElementPanel.
     */
    private void init()
    {
        try
        {
            flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.stringFlavor;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param df
     *            the flavor type desired for the data. Acceptable value is
     *            DataFlavor.stringFlavor.
     * @return if df is DataFlavor.stringFlavor, returns a raw string containing
     *         an Ant element name.
     */
    public Object getTransferData(DataFlavor df)
    {
        if (df == null)
            return null;

        if (data != null)
        {

            return _prefix + data + _suffix;
        }
        return null;
    }

    /**
     * @return an array containing a single ElementFlavor.
     */
    public DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

    /**
     * @param df
     *            the flavor to check
     * @return true if df is an ElementFlavor
     */
    public boolean isDataFlavorSupported(DataFlavor df)
    {
        if (df == null)
            return false;
        for (int i = 0; i < flavors.length; i++)
        {
            if (df.equals(flavors[i]))
            {
                return true;
            }
        }
        return false;
    }
}
