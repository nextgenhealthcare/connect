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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;

import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.*;
import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;

/**
 * The main mirth class.  Sets up the login and then authenticates
 * the login information and sets up Frame (the main application window).
 */
public class Mirth 
{
    public Client client;
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
        PlatformUI.MIRTH_FRAME.setupFrame(m);
        
        int width = UIConstants.MIRTH_WIDTH;
        int height = UIConstants.MIRTH_HEIGHT;
        
        if(userPreferences.getInt("maximizedState", PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH) != PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH)
        {
            width = userPreferences.getInt("width", UIConstants.MIRTH_WIDTH);    
            height = userPreferences.getInt("height", UIConstants.MIRTH_HEIGHT);
        }
        
        PlatformUI.MIRTH_FRAME.setSize(width,height);
        PlatformUI.MIRTH_FRAME.setLocationRelativeTo(null);
        
        if(userPreferences.getInt("maximizedState", PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH) == PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH)
            PlatformUI.MIRTH_FRAME.setExtendedState(PlatformUI.MIRTH_FRAME.MAXIMIZED_BOTH);
        
        PlatformUI.MIRTH_FRAME.setVisible(true);
        PlatformUI.MIRTH_FRAME.addComponentListener(new java.awt.event.ComponentAdapter() 
        {
            public void componentResized(ComponentEvent e) 
            {
                Frame tmp = (Frame)e.getSource();
                if (tmp.getWidth()<UIConstants.MIRTH_WIDTH) 
                {
                    tmp.setSize(UIConstants.MIRTH_WIDTH, tmp.getHeight());
                }
                if (tmp.getHeight()<UIConstants.MIRTH_HEIGHT) 
                {
                    tmp.setSize(tmp.getWidth(), UIConstants.MIRTH_HEIGHT);
                }
            }
        });
    }
    
    /**
     * Application entry point.  
     * Sets up the login panel and its layout as well.
     *
     * @param args String[]
     */
    public static void main(String[] args)
    {
        final String server;
        
        if(args.length > 0)
            server = args[0];
        else
            server = "https://localhost:8443";
        
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
                //UIManager.put("TabbedPane.selected", new Color(0xffffff));
                UIManager.put("TabbedPane.highlight", new Color(225,225,225));
                UIManager.put("TabbedPane.selectHighlight", new Color(0xc3c3c3));
                //UIManager.put("TabbedPane.background",new Color(225,225,225));
                //UIManager.put("TabbedPane.tabAreaBackground",new Color(225,225,225));
                UIManager.put("TabbedPane.contentBorderInsets", new InsetsUIResource(0,0,0,0));
                UIManager.put("TaskPaneContainer.backgroundGradientStart", new Color(0xc0d2dc));
                UIManager.put("TaskPaneContainer.backgroundGradientEnd", new Color(0x94b4c6));
                UIManager.put("TextPane.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TextPane.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ToggleButton.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("Panel.font",UIConstants.DIALOG_FONT);
                UIManager.put("PopupMenu.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("OptionPane.font",UIConstants.DIALOG_FONT);
                UIManager.put("Label.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("Tree.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ScrollPane.font",UIConstants.DIALOG_FONT);
                UIManager.put("TextField.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("Viewport.font",UIConstants.DIALOG_FONT);
                UIManager.put("MenuBar.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("FormattedTextField.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("DesktopIcon.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TableHeader.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ToolTip.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("PasswordField.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TaskPane.font",UIConstants.TEXTFIELD_BOLD_FONT);
                UIManager.put("Table.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TabbedPane.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ProgressBar.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("CheckBoxMenuItem.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ColorChooser.font",UIConstants.DIALOG_FONT);
                UIManager.put("Button.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TextArea.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("Spinner.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("RadioButton.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TitledBorder.font",UIConstants.TEXTFIELD_BOLD_FONT);
                UIManager.put("EditorPane.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("RadioButtonMenuItem.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ToolBar.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("MenuItem.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("CheckBox.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("JXTitledPanel.title.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("Menu.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("ComboBox.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("JXLoginPanel.banner.font",UIConstants.BANNER_FONT);
                UIManager.put("List.font",UIConstants.TEXTFIELD_PLAIN_FONT);
                UIManager.put("TaskPane.titleBackgroundGradientStart", new Color(0xffffff));
                UIManager.put("TaskPane.titleBackgroundGradientEnd",new Color(0xffffff));
                PlatformUI.BACKGROUND_IMAGE = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/header.jpg"));
                login = new LoginPanel(server);
            }
        });
    }
}

