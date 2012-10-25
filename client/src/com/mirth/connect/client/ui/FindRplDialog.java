/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.syntax.jedit.JEditTextArea;

public class FindRplDialog extends javax.swing.JDialog {

    static MyOwnFocusTraversalPolicy tabPolicy;
    private Frame parent;

    /** Creates new form FindRplDialog */
    public FindRplDialog(java.awt.Frame parent, boolean modal, JEditTextArea textarea) {
        super(parent, modal);
        initialize(textarea);
    }

    public FindRplDialog(Dialog parent, boolean modal, JEditTextArea textarea) {
        super(parent, modal);
        initialize(textarea);
    }

    private void initialize(JEditTextArea textarea) {
        initComponents();
        this.parent = PlatformUI.MIRTH_FRAME;
        search_text = textarea;
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        if (search_text.isEditable()) {
            this.enableReplace();
        } else {
            this.disableReplace();
        }
        // set the initial search field to what is highlighted
        textField_find.setText(search_text.getSelectedText());
        // tab order
        Vector<Component> order = new Vector<Component>(8);
        order.add(textField_find);
        order.add(textField_replaceWith);
        order.add(button_find);
        order.add(button_replace);
        order.add(button_replaceAll);
        order.add(button_close);
        order.add(checkBox_regularExpression);
        order.add(checkBox_matchCase);
        tabPolicy = new MyOwnFocusTraversalPolicy(order);
        setFocusTraversalPolicy(tabPolicy);

        textField_find.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    find(true);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    exit();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    void exit() {
        this.setVisible(false);
    }

    void replaceAll() {
        if (search_text.isEditable()) {
            boolean anyRemaining = true;
            search_text.select(0, 0);
            int min = Integer.MIN_VALUE;
            while (anyRemaining) {
                find(false);
                int start = search_text.getSelectionStart();
                int end = search_text.getSelectionEnd();
                if (start < end) {
                    search_text.setSelectedText(textField_replaceWith.getText());
                } else {
                    anyRemaining = false;
                }
                if (start <= min) {
                    break;
                } else {
                    min = start;
                }
            }
        }
    }

    void replace() {
        if (search_text.isEditable()) {
            // if no word is selected, the first 'replace' click will behave like FIND.
            // if there's already a selection, do REPLACE.
            if (search_text.getSelectedText() == null) {
                // search for the word.
                find(true);
            } else {
                int start = search_text.getSelectionStart();
                int end = search_text.getSelectionEnd();
                if (start < end) {
                    search_text.setSelectedText(textField_replaceWith.getText());
                }
            }
        }
    }

    void find(boolean wrapSearch) {
        String text = search_text.getText();
        String search = textField_find.getText();
        // check for case sensitive
        if (!checkBox_matchCase.isSelected()) {
            text = text.toLowerCase();
            search = search.toLowerCase();
        }
        if (checkBox_regularExpression.isSelected()) {
            // do regular expression
            Pattern p = Pattern.compile(search);
            Matcher m = p.matcher(text);
            if (m.find(search_text.getSelectionEnd())) {
                int position = m.start();
                String group = m.group();
                if (position > -1) {
                    search_text.select(position, position + group.length());
                }
            } else if (m.find(0)) {
                int position = m.start();
                String group = m.group();
                if (position > -1) {
                    search_text.select(position, position + group.length());
                }
            }
        } else {
            int position = text.indexOf(search, search_text.getSelectionEnd());
            if (position > -1) {
                search_text.select(position, position + search.length());
            } // if we are at the end. wrap and search from start
            else {
                if (wrapSearch) {
                    position = text.indexOf(search, 0);
                    if (position > -1) {
                        search_text.select(position, position + search.length());
                    }
                }
            }
        }
    }

    void disableReplace() {
        button_replace.setVisible(false);
        button_replaceAll.setVisible(false);
    }

    void enableReplace() {
        button_replace.setVisible(true);
        button_replaceAll.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        button_find = new javax.swing.JButton();
        button_replace = new javax.swing.JButton();
        button_replaceAll = new javax.swing.JButton();
        button_close = new javax.swing.JButton();
        checkBox_regularExpression = new javax.swing.JCheckBox();
        checkBox_matchCase = new javax.swing.JCheckBox();
        textField_find = new javax.swing.JTextField();
        textField_replaceWith = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Find Replace");
        setResizable(false);

        jLabel1.setText("Find text:");

        jLabel2.setText("Replace with:");

        button_find.setText("Find");
        button_find.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_findActionPerformed(evt);
            }
        });

        button_replace.setText("Replace");
        button_replace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_replaceActionPerformed(evt);
            }
        });

        button_replaceAll.setText("Replace All");
        button_replaceAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_replaceAllActionPerformed(evt);
            }
        });

        button_close.setText("Close");
        button_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_closeActionPerformed(evt);
            }
        });

        checkBox_regularExpression.setText("Regular Expression");

        checkBox_matchCase.setText("Match Case");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textField_replaceWith, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(checkBox_regularExpression)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(checkBox_matchCase)
                        .addGap(54, 54, 54))
                    .addComponent(textField_find, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(button_replace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(button_find, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(button_replaceAll))
                    .addComponent(button_close, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {button_close, button_find, button_replace, button_replaceAll});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(button_find)
                    .addComponent(textField_find, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(button_replace)
                    .addComponent(textField_replaceWith, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(checkBox_regularExpression, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button_replaceAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(button_close)
                    .addComponent(checkBox_matchCase, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button_findActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_findActionPerformed
        find(true);
    }//GEN-LAST:event_button_findActionPerformed

    private void button_replaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_replaceActionPerformed
        replace();
    }//GEN-LAST:event_button_replaceActionPerformed

    private void button_replaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_replaceAllActionPerformed
        replaceAll();
    }//GEN-LAST:event_button_replaceAllActionPerformed

    private void button_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_closeActionPerformed
        exit();
    }//GEN-LAST:event_button_closeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_close;
    private javax.swing.JButton button_find;
    private javax.swing.JButton button_replace;
    private javax.swing.JButton button_replaceAll;
    private javax.swing.JCheckBox checkBox_matchCase;
    private javax.swing.JCheckBox checkBox_regularExpression;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField textField_find;
    private javax.swing.JTextField textField_replaceWith;
    // End of variables declaration//GEN-END:variables
    JEditTextArea search_text;

    public static class MyOwnFocusTraversalPolicy
            extends FocusTraversalPolicy {

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
