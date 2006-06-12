package com.webreach.mirth.client;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.auth.JDBCLoginService;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;

/**
 * <p>Title: Mirth Beta Prototype</p>
 *
 * <p>Description: Mirth Beta Prototype</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: WebReach, Inc.</p>
 *
 * @author Gary Teichrow
 * @version 1.0
 */
public class Mirth 
{
    boolean packFrame = false;
    public Client client;
    /**
     * Construct and show the application.
     */
    public Mirth(Client m) 
    {
        Frame frame = new Frame(m);
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) 
        {
            frame.pack();
        } 
        else 
        {
            frame.validate();
        }

        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.addComponentListener(new java.awt.event.ComponentAdapter() 
        {
            public void componentResized(ComponentEvent e) 
            {
               Frame tmp = (Frame)e.getSource();
               if (tmp.getWidth()<800 || tmp.getHeight()<600) 
               {
                 tmp.setSize(800, 600);
               }
            }
        });
    }
    
    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                
                final JDBCLoginService svc = null; //new JDBCLoginService(null,null);
                final JXLoginPanel.JXLoginFrame frm = JXLoginPanel.showLoginFrame(svc);
                frm.getPanel().setBannerText("Mirth :: Login");
                for(int i = 0; i < frm.getPanel().getComponents().length; i++)
                    System.out.println(i + " " + frm.getPanel().getComponent(0).getFont());

                /*frm.getPanel().getLoginService().addLoginListener(new LoginListener(frm.getPanel())
                {

                    public void loginFailed(LoginEvent loginEvent)
                    {
                    }

                    public void loginStarted(LoginEvent loginEvent)
                    {
                    }

                    public void loginCanceled(LoginEvent loginEvent)
                    {
                    }

                    public void loginSucceeded(LoginEvent loginEvent)
                    {
                    }
                });*/
                
                frm.addWindowListener(new WindowAdapter()
                {
                    public void windowClosed(WindowEvent e)
                    {
                        //String username = String.valueOf(frm.getPanel().getUserName());
                        //String password =  String.valueOf(frm.getPanel().getPassword());
                        String username = "admin";
                        String password = "abc12345";
                        Client mirthClient;
                        
                        try
                        {
                            mirthClient = new Client("http://34.34.34.69:8080");
                            if(mirthClient.login(username,password))
                            {
                                new Mirth(mirthClient);
                            }
                        }
                        catch (ClientException ex)
                        {
                            System.out.println("Could not connect to server...");
                        }                                                
                        try
                        {
                            svc.getConnection().close();
                        }
                        catch (Exception ex)
                        {
                        }
                    }
                });
                frm.setVisible(true);
            }
        });
    }
}