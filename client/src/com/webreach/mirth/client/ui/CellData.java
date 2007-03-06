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

import javax.swing.ImageIcon;

/**
 * Holds an ImageIcon and a String value. These are used for a cell that has an
 * image in it. This class has accessor methods to get and set these values.
 */
public class CellData
{
    private ImageIcon icon;
    private String text;

    public CellData(ImageIcon icon, String text)
    {
        this.icon = icon;
        this.text = text;
    }

    public ImageIcon getIcon()
    {
        return icon;
    }

    public String getText()
    {
        return text;
    }

    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String toString()
    {
        return text;
    }

}
