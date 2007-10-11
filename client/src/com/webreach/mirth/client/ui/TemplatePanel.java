/*
 * TemplatePanel.java
 *
 * Created on September 11, 2007, 1:13 PM
 */

package com.webreach.mirth.client.ui;

import com.webreach.mirth.client.ui.beans.*;
import com.webreach.mirth.client.ui.editors.BoundPropertiesSheetDialog;
import com.webreach.mirth.model.MessageObject;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

/**
 *
 * @author  brendanh
 */
public class TemplatePanel extends javax.swing.JPanel
{
    public final String DEFAULT_TEXT = "Paste a sample message here.";
    
    private SyntaxDocument HL7Doc;
    
    private TreePanel treePanel;
    
    private String currentMessage = "";
    
    private String data;
    
    private Properties dataProperties;
    
    private Timer timer;
    
    /** Creates new form MessageTreeTemplate */   
    public TemplatePanel()
    {
        initComponents();
        
        dataType.setModel(new javax.swing.DefaultComboBoxModel(PlatformUI.MIRTH_FRAME.protocols.values().toArray()));
        
        HL7Doc = new SyntaxDocument();
        HL7Doc.setTokenMarker(new HL7TokenMarker());
        pasteBox.setDocument(HL7Doc);
        //  pasteBox.setPreferredSize(new Dimension(100,100));
        //  pasteBox.setFont(EditorConstants.DEFAULT_FONT);
        
        // handles updating the tree
        pasteBox.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                updateText();
            }
            
            public void insertUpdate(DocumentEvent e)
            {
                updateText();
            }
            
            public void removeUpdate(DocumentEvent e)
            {
                updateText();
            }
        });
        pasteBox.addMouseListener(new MouseListener()
        {
            
            public void mouseClicked(MouseEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getButton() == MouseEvent.BUTTON2)
                {
                    if (pasteBox.getText().equals(DEFAULT_TEXT))
                    {
                        pasteBox.setText("");
                    }
                }
            }
            
            public void mouseEntered(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void mouseExited(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void mousePressed(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void mouseReleased(MouseEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getButton() == MouseEvent.BUTTON2)
                {
                    if (pasteBox.getText().length() == 0)
                    {
                        pasteBox.setText(DEFAULT_TEXT);
                    }
                }
            }
            
        });
    }
    
    public void setDataTypeEnabled(boolean enabled)
    {
        dataType.setEnabled(enabled);
    }
    
    public void setTreePanel(TreePanel tree)
    {
        this.treePanel = tree;
    }
    
    private void updateText()
    {
        class UpdateTimer extends TimerTask
        {
            
            @Override
            public void run()
            {
                
                if (!currentMessage.equals(pasteBox.getText()))
                {
                    PlatformUI.MIRTH_FRAME.setWorking("Parsing...", true);
                    
                    String message = pasteBox.getText();
                    currentMessage = message;
                    treePanel.setMessage(dataProperties, (String) dataType.getSelectedItem(), message, DEFAULT_TEXT, dataProperties);
                    PlatformUI.MIRTH_FRAME.setWorking("", false);
                    
                }
            }
            
        }
        if (timer == null)
        {
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
        else
        {
            timer.cancel();
            PlatformUI.MIRTH_FRAME.setWorking("", false);
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
    }
   
    
    public String getMessage()
    {
        if (pasteBox.getText().equals(DEFAULT_TEXT))
            return "";
        else
            return pasteBox.getText().replace('\n', '\r');
    }
    
    public void setMessage(String msg)
    {
        if (msg != null)
            msg = msg.replace('\r', '\n');
        pasteBox.setText(msg);
        pasteBoxFocusLost(null);
        updateText();
    }
    
    public void clearMessage()
    {
        treePanel.clearMessage();
        pasteBoxFocusLost(null);
        updateText();
    }
    
    public void setProtocol(String protocol)
    {
        dataType.setSelectedItem(protocol);
        
        setDocType(protocol);
    }
    
    private void setDocType(String protocol)
    {
        if (protocol.equals("HL7 v2.x"))
        {
            HL7Doc.setTokenMarker(new HL7TokenMarker());
        }
        else if (protocol.equals("EDI"))
        {
            HL7Doc.setTokenMarker(new EDITokenMarker());
        }
        else if (protocol.equals("X12"))
        {
            HL7Doc.setTokenMarker(new X12TokenMarker());
        }
        else if (protocol.equals("HL7 v3.0") || protocol.equals("XML"))
        {
            HL7Doc.setTokenMarker(new XMLTokenMarker());
        }
        pasteBox.setDocument(HL7Doc);
    }
    
    public String getProtocol()
    {
        return (String) dataType.getSelectedItem();
    }
    
    public Properties getDataProperties()
    {
        return dataProperties;
    }
    
    public void setDataProperties(Properties p)
    {
        if (p != null)
            dataProperties = p;
        else
            dataProperties = new Properties();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jLabel5 = new javax.swing.JLabel();
        dataType = new javax.swing.JComboBox();
        properties = new javax.swing.JButton();
        pasteBox = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 1, 1), "Message Template", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 51)));
        jLabel5.setText("Data Type:");

        dataType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dataTypeActionPerformed(evt);
            }
        });

        properties.setText("Properties");
        properties.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                propertiesActionPerformed(evt);
            }
        });

        pasteBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pasteBox.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusGained(java.awt.event.FocusEvent evt)
            {
                pasteBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                pasteBoxFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pasteBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(properties)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(dataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(properties))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pasteBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void pasteBoxFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_pasteBoxFocusLost
    {//GEN-HEADEREND:event_pasteBoxFocusLost
        if (pasteBox.getText().length() == 0)
        {
            pasteBox.setText(DEFAULT_TEXT);
        }
    }//GEN-LAST:event_pasteBoxFocusLost
    
    private void pasteBoxFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_pasteBoxFocusGained
    {//GEN-HEADEREND:event_pasteBoxFocusGained
        if (pasteBox.getText().equals(DEFAULT_TEXT))
        {
            pasteBox.setText("");
        }
    }//GEN-LAST:event_pasteBoxFocusGained
    
    private void propertiesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_propertiesActionPerformed
    {//GEN-HEADEREND:event_propertiesActionPerformed
        PlatformUI.MIRTH_FRAME.enableSave();
        currentMessage = "";
        if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI)))
            new BoundPropertiesSheetDialog(dataProperties, new EDIProperties());
        else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12)))
            new BoundPropertiesSheetDialog(dataProperties, new X12Properties());
        else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2)))
            new BoundPropertiesSheetDialog(dataProperties, new HL7Properties());
        else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP)))
            new BoundPropertiesSheetDialog(dataProperties, new NCPDPProperties());
        updateText();
    }//GEN-LAST:event_propertiesActionPerformed
    
    private void dataTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeActionPerformed
    {//GEN-HEADEREND:event_dataTypeActionPerformed
        PlatformUI.MIRTH_FRAME.enableSave();
        currentMessage = "";
        if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12)) ||
                ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI)) ||
                ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2)) ||
                ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP)))
            properties.setEnabled(true);
        else
            properties.setEnabled(false);
        dataProperties = new Properties();
        setDocType((String)dataType.getSelectedItem());
        updateText();
    }//GEN-LAST:event_dataTypeActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dataType;
    private javax.swing.JLabel jLabel5;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea pasteBox;
    private javax.swing.JButton properties;
    // End of variables declaration//GEN-END:variables
    
}
