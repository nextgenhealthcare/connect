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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;
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
                    PlasticLookAndFeel.setPlasticTheme(new Silver());
                    PlasticXPLookAndFeel look = new PlasticXPLookAndFeel();
                    UIManager.setLookAndFeel(look);
                    UIManager.put("win.xpstyle.name", "metallic");
                    LookAndFeelAddons.setAddon(WindowsLookAndFeelAddons.class);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                String userDefault = "";
                String passwordDefault = "";
                String mirthServerDefault = server;
                
                UIManager.put("JXLoginPanel.banner.foreground", UIConstants.TITLE_TEXT_COLOR);
                UIManager.put("JXLoginPanel.banner.darkBackground", UIConstants.BANNER_DARK_BACKGROUND);
                UIManager.put("JXLoginPanel.banner.lightBackground", UIConstants.BANNER_LIGHT_BACKGROUND);
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

                
                final MirthLoginService svc = new MirthLoginService();
                JXLoginPanel panel = new JXLoginPanel(svc, null, null, null);
                
                panel.setBannerText("");
                PlatformUI.BACKGROUND_IMAGE = new ImageIcon(panel.getUI().getBanner());
                
                panel.setBannerText("Login :: Mirth");
                panel.setOpaque(true);
                JPanel loginInfo = (JPanel)((JPanel)panel.getComponent(1)).getComponent(1);

                loginInfo.removeAll();
                
                String CLASS_NAME = JXLoginPanel.class.getCanonicalName(); 
                JLabel serverLabel = new JLabel("Server");
                javax.swing.JTextField serverName = new javax.swing.JTextField(mirthServerDefault, 30); 
                JLabel nameLabel = new JLabel("Login"); 
                JLabel passwordLabel = new JLabel("Password");  
                javax.swing.JTextField nameField = new javax.swing.JTextField(userDefault, 30); 
                JPasswordField passwordField = new JPasswordField(passwordDefault, 30);  
                               
                loginInfo.setLayout(new GridBagLayout());  
                
                GridBagConstraints gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 0;  
                gridBagConstraints.gridy = 0;  
                gridBagConstraints.anchor = GridBagConstraints.EAST;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 11);  
                loginInfo.add(serverLabel, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 1;  
                gridBagConstraints.gridy = 0;  
                gridBagConstraints.gridwidth = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.weightx = 1.0;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 0);  
                loginInfo.add(serverName, gridBagConstraints);  
                
                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 0;  
                gridBagConstraints.gridy = 1;  
                gridBagConstraints.anchor = GridBagConstraints.EAST;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 11);  
                loginInfo.add(nameLabel, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 1;  
                gridBagConstraints.gridy = 1;  
                gridBagConstraints.gridwidth = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.weightx = 1.0;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 0);  
                loginInfo.add(nameField, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 0;  
                gridBagConstraints.gridy = 2;  
                gridBagConstraints.anchor = GridBagConstraints.EAST;  
                gridBagConstraints.insets = new Insets(0, 0, 11, 11);  
                loginInfo.add(passwordLabel, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 2;  
                gridBagConstraints.gridwidth = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new Insets(0, 0, 11, 0);  
                loginInfo.add(passwordField, gridBagConstraints);  
                
                loginInfo.getComponent(5).setFont(loginInfo.getComponent(1).getFont());
                svc.setPanel(loginInfo);
                svc.addLoginListener(new MirthLoginListener());
                
                final JXLoginPanel.JXLoginFrame frm = JXLoginPanel.showLoginFrame(panel);
                frm.setIconImage(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/emoticon_smile.png")).getImage());
                frm.setTitle("Mirth Administrator Login");
                frm.setVisible(true);
                nameField.grabFocus();
                
                frm.addWindowListener(new WindowAdapter()
                {
                    public void windowClosed(WindowEvent e)
                    {
                        if(svc.getMirth() == null)
                            System.exit(0);
                    }
                });
            }
        });
    }
}

/**
 * A listener for logging in.
 */
class MirthLoginListener implements LoginListener 
{
        public MirthLoginListener() 
        {
        }
        public void loginSucceeded(LoginEvent source) 
        {
        }
        public void loginStarted(LoginEvent source) 
        {
        }
        public void loginFailed(LoginEvent source) 
        {
        }
        public void loginCanceled(LoginEvent source) 
        {
            System.exit(0);
        }
}

/**
 * A login service that authenticates login information.
 */
class MirthLoginService extends LoginService 
{
        Client client;
        JPanel loginPanel;
        Mirth mirth = null;
        
        public MirthLoginService() 
        {
        }
        
        public void setPanel(JPanel p)
        {
            this.loginPanel = p;
        }
        
        public Client getClient()
        {
            return client;
        }
        
        public Mirth getMirth()
        {
            return mirth;
        }
        
        public boolean authenticate(final String username, char[] pass, String server) throws Exception 
        {
            String user = ((javax.swing.JTextField)loginPanel.getComponent(3)).getText();
            String pw =  ((javax.swing.JTextField)loginPanel.getComponent(5)).getText();
            String mirthServer = ((javax.swing.JTextField)loginPanel.getComponent(1)).getText();
            client = new Client(mirthServer);
            try
            {
                if(client.login(user,pw))
                {
                    try{
                        PlatformUI.USER_NAME = user;
                        PlatformUI.SERVER_NAME = mirthServer;
                        mirth = new Mirth(client);
                        return true;
                    }catch(Throwable t){
                        System.out.println("Error starting the Frame: "+t);
                        t.printStackTrace();
                    }
                }
            }
            catch (ClientException ex)
            {
                System.out.println("Could not connect to server...");
            }      
            return false;
        }
}
