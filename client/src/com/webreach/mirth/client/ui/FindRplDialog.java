/*
 * FindRplDialog.java
 *
 * Created on November 1, 2007, 3:29 PM
 */

package com.webreach.mirth.client.ui;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.syntax.jedit.JEditTextArea;


/**
 *
 * @author  dans
 */
public class FindRplDialog extends javax.swing.JDialog {
    static MyOwnFocusTraversalPolicy tabPolicy;

    /** Creates new form FindRplDialog */
    public FindRplDialog(Frame parent, boolean modal, JEditTextArea textarea) {
        super(parent, modal);
        initComponents();
        
        search_text = textarea;
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();
        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        if(search_text.isEditable()){
            this.enableReplace();
        }
        else {
            this.disableReplace();
        }
        // set the initial search field to what is highlighted
        mirthTextField1.setText(search_text.getSelectedText());
        // tab order
        Vector<Component> order = new Vector<Component>(8);
        order.add(mirthTextField1);
        order.add(mirthTextField2);
        order.add(mirthButton1);
        order.add(mirthButton2);
        order.add(mirthButton3);
        order.add(mirthButton4);
        order.add(mirthCheckBox1);
        order.add(mirthCheckBox2);
        tabPolicy = new MyOwnFocusTraversalPolicy(order);
        setFocusTraversalPolicy(tabPolicy);

        mirthTextField1.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER){
					find(true);
				}else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
					exit();
				}
				
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}});    
    }
    void exit(){
    	this.setVisible(false);
    }
    void replaceAll(){
        if(search_text.isEditable()){
            boolean anyRemaining = true;
            search_text.select(0,0);
            int min =  Integer.MIN_VALUE;
            while(anyRemaining){
                find(false);
                int start = search_text.getSelectionStart();
                int end = search_text.getSelectionEnd();
                if(start < end){
                    search_text.setSelectedText(mirthTextField2.getText());
                }
                else {
                    anyRemaining = false;
                }
                if(start <= min){
                    break;
                }
                else {
                    min = start;
                }

            }
        }
    }
    void replace(){
        if(search_text.isEditable()){
            find(true);
            int start = search_text.getSelectionStart();
            int end = search_text.getSelectionEnd();
            if(start < end){
                search_text.setSelectedText(mirthTextField2.getText());
            }
        }
    }    
    void find(boolean wrapSearch){
        String text = search_text.getText();
        String search = mirthTextField1.getText();
        // check for case sensitive
        if(!mirthCheckBox2.isSelected()){
            text = text.toLowerCase();
            search = search.toLowerCase();
        }
        if(mirthCheckBox1.isSelected()){
            // do regular expression
            Pattern p = Pattern.compile(search);
            Matcher m = p.matcher(text);
            if(m.find(search_text.getSelectionEnd())){
                int position = m.start();
                String group = m.group();
                if(position > -1){
                    search_text.select(position,position+group.length());
                }
            }
            else if(m.find(0)){
                int position = m.start();
                String group = m.group();
                if(position > -1){
                    search_text.select(position,position+group.length());
                }
            }
        }
        else {
            int position = text.indexOf(search,search_text.getSelectionEnd());
            if(position > -1){
                search_text.select(position,position+search.length());
            }
            // if we are at the end. wrap and search from start
            else {
                if(wrapSearch){
                    position = text.indexOf(search, 0);
                    if(position > -1){
                        search_text.select(position,position+search.length());
                    }
                }
            }
        }
    }


    void disableReplace(){
        mirthButton2.hide();
        mirthButton3.hide();
    }
    void enableReplace(){
        mirthButton2.show();
        mirthButton3.show();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        mirthTextField1 = new com.webreach.mirth.client.ui.components.MirthTextField();
        mirthTextField2 = new com.webreach.mirth.client.ui.components.MirthTextField();
        mirthButton1 = new com.webreach.mirth.client.ui.components.MirthButton();
        mirthButton2 = new com.webreach.mirth.client.ui.components.MirthButton();
        mirthButton3 = new com.webreach.mirth.client.ui.components.MirthButton();
        mirthButton4 = new com.webreach.mirth.client.ui.components.MirthButton();
        mirthCheckBox1 = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        mirthCheckBox2 = new com.webreach.mirth.client.ui.components.MirthCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Find Replace");
        setAlwaysOnTop(true);
        setResizable(false);
        jLabel1.setText("Find text:");

        jLabel2.setText("Replace with:");

        mirthButton1.setText("Find");
        mirthButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mirthButton1ActionPerformed(evt);
            }
        });

        mirthButton2.setText("Replace");
        mirthButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mirthButton2ActionPerformed(evt);
            }
        });

        mirthButton3.setText("Replace All");
        mirthButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mirthButton3ActionPerformed(evt);
            }
        });

        mirthButton4.setText("Close");
        mirthButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mirthButton4ActionPerformed(evt);
            }
        });

        mirthCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthCheckBox1.setText("Regular Expression");
        mirthCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mirthCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthCheckBox2.setText("Match Case");
        mirthCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1))
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mirthCheckBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(mirthCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 32, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, mirthTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, mirthTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(mirthButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(mirthButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(mirthButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(mirthButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mirthTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mirthButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mirthTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mirthButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(mirthCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mirthButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mirthCheckBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mirthButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mirthButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirthButton4ActionPerformed
        //close
        setVisible(false);
    }//GEN-LAST:event_mirthButton4ActionPerformed

    private void mirthButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirthButton3ActionPerformed
        //replace all
        replaceAll();
    }//GEN-LAST:event_mirthButton3ActionPerformed

    private void mirthButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirthButton2ActionPerformed
        // replace
        replace();
    }//GEN-LAST:event_mirthButton2ActionPerformed

    private void mirthButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirthButton1ActionPerformed
        // find
        find(true);
    }//GEN-LAST:event_mirthButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new FindRplDialog(new javax.swing.JFrame(), true,new JEditTextArea()).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private com.webreach.mirth.client.ui.components.MirthButton mirthButton1;
    private com.webreach.mirth.client.ui.components.MirthButton mirthButton2;
    private com.webreach.mirth.client.ui.components.MirthButton mirthButton3;
    private com.webreach.mirth.client.ui.components.MirthButton mirthButton4;
    private com.webreach.mirth.client.ui.components.MirthCheckBox mirthCheckBox1;
    private com.webreach.mirth.client.ui.components.MirthCheckBox mirthCheckBox2;
    private com.webreach.mirth.client.ui.components.MirthTextField mirthTextField1;
    private com.webreach.mirth.client.ui.components.MirthTextField mirthTextField2;
    // End of variables declaration//GEN-END:variables
   JEditTextArea search_text;


    public static class MyOwnFocusTraversalPolicy
                  extends FocusTraversalPolicy
    {
        Vector<Component> order;

        public MyOwnFocusTraversalPolicy(Vector<Component> order) {
            this.order = new Vector<Component>(order.size());
            this.order.addAll(order);
        }
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            int idx = (order.indexOf(aComponent) + 1) % order.size();
            return order.get(idx);
        }
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int idx = order.indexOf(aComponent) - 1;
            if (idx < 0) {
                idx = order.size() - 1;
            }
            return order.get(idx);
        }
        public Component getDefaultComponent(Container focusCycleRoot) {
            return order.get(0);
        }
        public Component getLastComponent(Container focusCycleRoot) {
            return order.lastElement();
        }
        public Component getFirstComponent(Container focusCycleRoot) {
            return order.get(0);
        }
    }
}
