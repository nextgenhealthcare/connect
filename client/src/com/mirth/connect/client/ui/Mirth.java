/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.JTextComponent.KeyBinding;

import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.mirth.connect.client.core.Client;

/**
 * The main mirth class. Sets up the login and then authenticates the login
 * information and sets up Frame (the main application window).
 */
public class Mirth {

    private static Preferences userPreferences;

    /**
     * Construct and show the application.
     */
    public Mirth(Client mirthClient) {
        PlatformUI.MIRTH_FRAME = new Frame();

        UIManager.put("Tree.leafIcon", UIConstants.LEAF_ICON);
        UIManager.put("Tree.openIcon", UIConstants.OPEN_ICON);
        UIManager.put("Tree.closedIcon", UIConstants.CLOSED_ICON);

        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        LoginPanel.getInstance().setStatus("Loading components...");
        PlatformUI.MIRTH_FRAME.setupFrame(mirthClient);

        int width = UIConstants.MIRTH_WIDTH;
        int height = UIConstants.MIRTH_HEIGHT;

        if (userPreferences.getInt("maximizedState", Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
            width = userPreferences.getInt("width", UIConstants.MIRTH_WIDTH);
            height = userPreferences.getInt("height", UIConstants.MIRTH_HEIGHT);
        }

        PlatformUI.MIRTH_FRAME.setSize(width, height);
        PlatformUI.MIRTH_FRAME.setLocationRelativeTo(null);

        if (userPreferences.getInt("maximizedState", Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            PlatformUI.MIRTH_FRAME.setExtendedState(Frame.MAXIMIZED_BOTH);
        }

        PlatformUI.MIRTH_FRAME.setVisible(true);
    }
    
    /**
     * About menu item on Mac OS X
     */
    public static void aboutMac() {
        new AboutMirth();
    }
    
	/**
	 * Quit menu item on Mac OS X. Only exit if on the login window, or if
	 * logout is successful
	 * 
	 * @return quit
	 */
    public static boolean quitMac() {
    	return (LoginPanel.getInstance().isVisible() || (PlatformUI.MIRTH_FRAME != null && PlatformUI.MIRTH_FRAME.logout()));
    }

    private static void createMacKeyBindings() {
        int acceleratorKey = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        // Add the common KeyBindings for macs
        KeyBinding[] defaultBindings = {
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C, acceleratorKey), DefaultEditorKit.copyAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V, acceleratorKey), DefaultEditorKit.pasteAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_X, acceleratorKey), DefaultEditorKit.cutAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_A, acceleratorKey), DefaultEditorKit.selectAllAction),
            // deleteNextWordAction and deletePrevWordAction not available in Java 1.5
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, acceleratorKey), DefaultEditorKit.deleteNextWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, acceleratorKey), DefaultEditorKit.deletePrevWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, acceleratorKey), DefaultEditorKit.nextWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, acceleratorKey), DefaultEditorKit.nextWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, acceleratorKey), DefaultEditorKit.previousWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, acceleratorKey), DefaultEditorKit.previousWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, acceleratorKey | InputEvent.SHIFT_MASK), DefaultEditorKit.selectionNextWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, acceleratorKey | InputEvent.SHIFT_MASK), DefaultEditorKit.selectionNextWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, acceleratorKey | InputEvent.SHIFT_MASK), DefaultEditorKit.selectionPreviousWordAction),
            new KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, acceleratorKey | InputEvent.SHIFT_MASK), DefaultEditorKit.selectionPreviousWordAction)
        };

        keyMapBindings(new javax.swing.JTextField(), defaultBindings);
        keyMapBindings(new javax.swing.JEditorPane(), defaultBindings);
        keyMapBindings(new javax.swing.JTextArea(), defaultBindings);
        keyMapBindings(new com.mirth.connect.client.ui.components.MirthTextArea(), defaultBindings);
        keyMapBindings(new com.mirth.connect.client.ui.components.MirthTextField(), defaultBindings);
        keyMapBindings(new com.mirth.connect.client.ui.components.MirthTextPane(), defaultBindings);
    }

    private static void keyMapBindings(JTextComponent comp, KeyBinding[] bindings) {
        JTextComponent.loadKeymap(comp.getKeymap(), bindings, comp.getActions());
    }

    /**
     * Application entry point. Sets up the login panel and its layout as well.
     * 
     * @param args
     *            String[]
     */
    public static void main(String[] args) {
        final String server;
        final String version;
        final String username;
        final String password;

        if (args.length == 2) {
            server = args[0];
            version = args[1];
            username = "";
            password = "";
        } else if (args.length == 3) {
            server = args[0];
            version = args[1];
            username = args[2];
            password = "";
        } else if (args.length == 4) {
            server = args[0];
            version = args[1];
            username = args[2];
            password = args[3];
        } else {
            server = "https://localhost:8443";
            version = "";
            username = "";
            password = "";
        }
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    PlasticLookAndFeel.setPlasticTheme(new MirthTheme());
                    PlasticXPLookAndFeel look = new PlasticXPLookAndFeel();
                    UIManager.setLookAndFeel(look);
                    UIManager.put("win.xpstyle.name", "metallic");
                    LookAndFeelAddons.setAddon(WindowsLookAndFeelAddons.class);
                    if (System.getProperty("os.name").toLowerCase().lastIndexOf("mac") != -1) {
                        createMacKeyBindings();
                        OSXAdapter.setAboutHandler(Mirth.class, Mirth.class.getDeclaredMethod("aboutMac", (Class[]) null));
                        OSXAdapter.setQuitHandler(Mirth.class, Mirth.class.getDeclaredMethod("quitMac", (Class[]) null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // keep the tooltips from disappearing
                ToolTipManager.sharedInstance().setDismissDelay(3600000);
                
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

                try {
                    UIManager.put("wizard.sidebar.image", ImageIO.read(com.mirth.connect.client.ui.Frame.class.getResource("images/wizardsidebar.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlatformUI.BACKGROUND_IMAGE = new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/header_nologo.png"));
                LoginPanel.getInstance().initialize(server, version, username, password);
            }
        });
    }
}
