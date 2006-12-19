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

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

/**
 * A constants class for the Mirth UI
 */
public class UIConstants
{    
    // for EOL stuff
    public static final String EOL_JAVA = "\n";
    public static final String EOL_UNIX = "\n";
    public static final String EOL_WIN32 = "\r\n";
    public static final String EOL_MAC = "\r";

    // for Frame
    public static final int TASK_PANE_WIDTH = 170;
    public static final String TITLE_TEXT = "Mirth Administrator";
    public static final int MIRTH_WIDTH = 950;
    public static final int MIRTH_HEIGHT = 650;

    // for error checking
    public static final int ERROR_CONSTANT = -1;

    // for JXTables
    public static final int ROW_HEIGHT = 20;
    public static final int COL_MARGIN = 10;
    public static final Color GRID_COLOR = new Color(224,224,224);
    public static final int MAX_WIDTH = 200;
    public static final Color HIGHLIGHTER_COLOR = new Color(240,240,240);

    // background colors
    public static final Color BACKGROUND_COLOR = new Color(255,255,255);
    public static final Color TITLE_TEXT_COLOR = new Color(0,0,0);
    public static final Color HEADER_TITLE_TEXT_COLOR = new Color(255,255,255);
    public static final Color BANNER_DARK_BACKGROUND = new Color(170,170,170);
    public static final Color BANNER_LIGHT_BACKGROUND = new Color(220,220,220);
    public static final Color NONEDITABLE_LINE_BACKGROUND = new Color(255,255,224);

    // for JSplitPane
    public static final int DIVIDER_SIZE = 12;

    // fonts
    public static final Font TEXTFIELD_PLAIN_FONT = new Font("Tahoma",Font.PLAIN,11);
    public static final Font TEXTFIELD_BOLD_FONT = new Font("Tahoma",Font.BOLD,11);
    public static final Font BANNER_FONT = new Font("Arial", Font.BOLD, 36);
    public static final Font DIALOG_FONT = new Font("Dialog", Font.PLAIN, 12);

    // help link
    public static final String HELP_LOCATION = "http://www.mirthproject.org/index.php?option=com_jd-wiki&Itemid=44&id=";
    public static final String CHANNEL_HELP_LOCATION = "channeleditor";
    public static final String TRANFORMER_HELP_LOCATION = "transformereditor";
    public static final String FILTER_HELP_LOCATION = "filtereditor";
    public static final String CHANNELS_HELP_LOCATION = "channellist";
    public static final String STATUS_HELP_LOCATION = "statuslist";
    public static final String MESSAGE_BROWSER_HELP_LOCATION = "messagebrowser";
    public static final String SYSTEM_EVENT_HELP_LOCATION = "systemevents";
    public static final String ADMIN_HELP_LOCATION = "administration";

    // for JTrees
    public static final ImageIcon LEAF_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_green.png"));
    public static final ImageIcon OPEN_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_yellow.png"));
    public static final ImageIcon CLOSED_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_yellow.png"));

    // for Forms
    public static final String YES_OPTION = "1";
    public static final String NO_OPTION = "0";
    
    //ast: encodings    
    public static final String DEFAULT_ENCODING_OPTION = "DEFAULT_ENCODING";
    public static final String UTF8_OPTION = "UTF-8";
    public static final String UTF16LE_OPTION = "UTF-16LE";
    public static final String UTF16BE_OPTION = "UTF-16BE";
    public static final String UTF16BOM_OPTION = "UTF-16";
    public static final String LATIN1_OPTION = "ISO-8859-1";
    public static final String USASCII_OPTION = "US-ASCII";
}
