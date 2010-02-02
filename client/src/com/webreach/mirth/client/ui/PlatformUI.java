package com.webreach.mirth.client.ui;

import javax.swing.ImageIcon;

import org.jdesktop.swingx.table.ColumnHeaderRenderer;

/**
 * A class of static variables that need to be referenced from multiple
 * locations.
 */
public class PlatformUI {

    public static Frame MIRTH_FRAME;
    public static ImageIcon BACKGROUND_IMAGE;
    public static String SERVER_NAME;
    public static String SERVER_ID;
    public static String USER_NAME;
    public static ColumnHeaderRenderer CENTER_COLUMN_HEADER_RENDERER = ColumnHeaderRenderer.createColumnHeaderRenderer();
    public static String CLIENT_VERSION;
    public static String SERVER_VERSION;
    public static String BUILD_DATE;
    public static String HELP_LOCATION = "http://www.mirthcorp.com/community/wiki/display/mirthuserguide";
}
