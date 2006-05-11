package com.webreach.mirth.client;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.auth.JDBCLoginService;
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
public class Mirth {
    boolean packFrame = false;

    /**
     * Construct and show the application.
     */
    public Mirth() {
        Frame frame = new Frame();
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
           Frame tmp = (Frame)e.getSource();
           if (tmp.getWidth()<800 || tmp.getHeight()<600) {
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
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.
                                             getSystemLookAndFeelClassName());
                    //UIManager.put("win.xpstyle.name", "luna");
                    //LookAndFeelAddons.setAddon(WindowsLookAndFeelAddons.class);
                    /*UIManager.put("TaskPaneContainer.useGradient",
                    Boolean.FALSE);
                    UIManager.put("TaskPaneContainer.backgroundGradientStart",
                    new Color(116,149,226));
                    UIManager.put("TaskPaneContainer.backgroundGradientEnd",
                    new Color(18,71,113).darker().darker());
                    UIManager.put("TaskPane.background",
                    Color.WHITE.darker());*/
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                final JDBCLoginService svc = new JDBCLoginService(
                "sun.jdbc.odbc.JdbcOdbcDriver",
                "jdbc:odbc:northwind");
                final JXLoginPanel.JXLoginFrame frm = JXLoginPanel.showLoginFrame(svc);
                frm.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent e) {
                        JXLoginPanel.Status status = frm.getStatus();
                        status = JXLoginPanel.Status.SUCCEEDED;
                        if (status == JXLoginPanel.Status.SUCCEEDED) {
                            new Mirth();
                        } else {
                            System.out.println("Login Failed: " + status);
                        }
                        try {svc.getConnection().close();} catch (Exception ex) {} 
                    }
                });
                frm.setVisible(true);
            }
        });
    }
}