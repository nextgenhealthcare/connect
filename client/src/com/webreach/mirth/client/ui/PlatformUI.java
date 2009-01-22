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

import org.jdesktop.swingx.table.ColumnHeaderRenderer;

/**
 * A class of static variables that need to be referenced from multiple
 * locations.
 */
public class PlatformUI
{
    public static Frame MIRTH_FRAME;
    public static ImageIcon BACKGROUND_IMAGE;
    public static String SERVER_NAME;
    public static String SERVER_ID;
    public static String USER_NAME;
    public static ColumnHeaderRenderer CENTER_COLUMN_HEADER_RENDERER = ColumnHeaderRenderer.createColumnHeaderRenderer();
    public static String CLIENT_VERSION;
    public static String SERVER_VERSION;
    public static String BUILD_DATE;
    public static String HELP_LOCATION = "http://www.webreachinc.com/wiki/display/mirthuserguide";
}
