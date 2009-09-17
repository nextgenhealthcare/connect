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
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;

import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;
import org.jdesktop.swingx.table.ColumnHeaderRenderer;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.webreach.mirth.client.core.Client;

/**
 * The main mirth class. Sets up the login and then authenticates the login
 * information and sets up Frame (the main application window).
 */
public class Mirth
{
    private static Preferences userPreferences;
    private static LoginPanel login;

    /**
     * Construct and show the application.
     */
    public Mirth(Client m)
    {
        PlatformUI.MIRTH_FRAME = new Frame();
        PlatformUI.CENTER_COLUMN_HEADER_RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
        PlatformUI.CENTER_COLUMN_HEADER_RENDERER.setDownIcon(UIManager.getIcon("ColumnHeaderRenderer.downIcon"));
        PlatformUI.CENTER_COLUMN_HEADER_RENDERER.setUpIcon(UIManager.getIcon("ColumnHeaderRenderer.upIcon"));

        UIManager.put("Tree.leafIcon", UIConstants.LEAF_ICON);
        UIManager.put("Tree.openIcon", UIConstants.OPEN_ICON);
        UIManager.put("Tree.closedIcon", UIConstants.CLOSED_ICON);

        userPreferences = Preferences.systemNodeForPackage(Mirth.class);        
        login.setStatus("Loading components...");
        PlatformUI.MIRTH_FRAME.setupFrame(m, login);

        int width = UIConstants.MIRTH_WIDTH;
        int height = UIConstants.MIRTH_HEIGHT;

        if (userPreferences.getInt("maximizedState", PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH) != PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH)
        {
            width = userPreferences.getInt("width", UIConstants.MIRTH_WIDTH);
            height = userPreferences.getInt("height", UIConstants.MIRTH_HEIGHT);
        }

        PlatformUI.MIRTH_FRAME.setSize(width, height);
        PlatformUI.MIRTH_FRAME.setLocationRelativeTo(null);

        if (userPreferences.getInt("maximizedState", PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH) == PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH)
            PlatformUI.MIRTH_FRAME.setExtendedState(PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH);

        PlatformUI.MIRTH_FRAME.setVisible(true);
    }

    /**
     * Application entry point. Sets up the login panel and its layout as well.
     * 
     * @param args
     *            String[]
     */
    public static void main(String[] args)
    {
        final String server;
        final String version;
        final String username;
        final String password;
        
        if (args.length == 2)
        {
            server = args[0];
            version = args[1];
            username = "";
            password = "";
        }
        else if(args.length == 3)
        {
            server = args[0];
            version = args[1];
            username = args[2];
            password = "";
        }
        else if(args.length == 4)
        {
            server = args[0];
            version = args[1];
            username = args[2];
            password = args[3];
        }
        else
        {
            server = "https://localhost:8443";
            version = "";
            username = "";
            password = "";
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    PlasticLookAndFeel.setPlasticTheme(new MirthTheme());
                    PlasticXPLookAndFeel look = new PlasticXPLookAndFeel();
                    UIManager.setLookAndFeel(look);
                    UIManager.put("win.xpstyle.name", "metallic");
                    LookAndFeelAddons.setAddon(WindowsLookAndFeelAddons.class);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                // TabbedPane defaults
                // UIManager.put("TabbedPane.selected", new Color(0xffffff));
                // UIManager.put("TabbedPane.background",new Color(225,225,225));
                // UIManager.put("TabbedPane.tabAreaBackground",new Color(225,225,225));
                UIManager.put("TabbedPane.highlight", new Color(225, 225, 225));
                UIManager.put("TabbedPane.selectHighlight", new Color(0xc3c3c3));
                UIManager.put("TabbedPane.contentBorderInsets", new InsetsUIResource(0, 0, 0, 0));

                // TaskPane defaults
                UIManager.put("TaskPane.titleBackgroundGradientStart", new Color(0xffffff));
                UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(0xffffff));
                
                // Set fonts
				UIManager.put("TextPane.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ToggleButton.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("Panel.font", UIConstants.DIALOG_FONT);
				UIManager.put("PopupMenu.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("OptionPane.font", UIConstants.DIALOG_FONT);
				UIManager.put("Label.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("Tree.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ScrollPane.font", UIConstants.DIALOG_FONT);
				UIManager.put("TextField.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("Viewport.font", UIConstants.DIALOG_FONT);
				UIManager.put("MenuBar.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("FormattedTextField.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("DesktopIcon.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("TableHeader.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ToolTip.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("PasswordField.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("TaskPane.font", UIConstants.TEXTFIELD_BOLD_FONT);
				UIManager.put("Table.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("TabbedPane.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ProgressBar.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("CheckBoxMenuItem.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ColorChooser.font", UIConstants.DIALOG_FONT);
				UIManager.put("Button.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("TextArea.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("Spinner.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("RadioButton.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("TitledBorder.font", UIConstants.TEXTFIELD_BOLD_FONT);
				UIManager.put("EditorPane.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("RadioButtonMenuItem.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ToolBar.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("MenuItem.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("CheckBox.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("JXTitledPanel.title.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("Menu.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("ComboBox.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				UIManager.put("JXLoginPanel.banner.font", UIConstants.BANNER_FONT);
				UIManager.put("List.font", UIConstants.TEXTFIELD_PLAIN_FONT);
				
				// Problem with JGoodies and JXTable: http://forums.java.net/jive/thread.jspa?messageID=278977
				UIManager.put(ColumnHeaderRenderer.VISTA_BORDER_HACK, UIManager.get("TableHeader.cellBorder"));
                
                try {
        			UIManager.put("wizard.sidebar.image", ImageIO.read(com.webreach.mirth.client.ui.Frame.class.getResource("images/wizardsidebar.png")));
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                PlatformUI.BACKGROUND_IMAGE = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/header_nologo.png"));
                login = new LoginPanel(server, version, username, password);
            }
        });
    }
}
