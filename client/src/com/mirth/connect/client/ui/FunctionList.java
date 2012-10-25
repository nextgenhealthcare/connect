/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory;
import com.mirth.connect.client.ui.panels.reference.ReferenceListPanel;
import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory.ListType;
import com.mirth.connect.model.CodeTemplate;

public class FunctionList extends javax.swing.JPanel {

    private LinkedHashMap<String, JPanel> panels;
    private int context;
    private boolean enableFilter;

    public FunctionList() {
        initComponents();
        this.enableFilter = true;
    }

    /** Creates new form FunctionList */
    public FunctionList(int context) {
    	this.enableFilter = true;
        this.context = context;
        initComponents();
        panels = new LinkedHashMap<String, JPanel>();
        setup();
        enableFilter(enableFilter);
    }

    /**
     * Constructor to initialize the panel with the option to enable/disable
     * the filter component
     *
     * @param context
     * @param enableFilter TRUE the panel should be initialized with the filter component
     */
    public FunctionList(int context, boolean enableFilter) {
        this(context);
        this.enableFilter = enableFilter;
        enableFilter(enableFilter);
    }

    /**
     * Helper method to disable/enable the filter components for this panel.
     *
     * @param enable TRUE to enable filter component to the panel.
     */
    private void enableFilter(boolean enable) {
    	enableFilter = enable;
    	
        filterLabel.setVisible(enable);
        filterField.setVisible(enable);
        filterField.setEnabled(enable);
    }

    public void setup() {
        ReferenceListFactory builder = ReferenceListFactory.getInstance();

        LinkedHashMap<String, ArrayList<CodeTemplate>> references = builder.getReferences();
        Iterator<Entry<String, ArrayList<CodeTemplate>>> i = references.entrySet().iterator();
        addPanel(new ReferenceListPanel(ListType.ALL.getValue(), builder.getVariableListItems(ListType.ALL.getValue(), context)), "All");

        while (i.hasNext()) {
            Entry<String, ArrayList<CodeTemplate>> entry = i.next();
            String key = entry.getKey();
            references.get(entry.getKey());
            ArrayList<CodeTemplate> items = builder.getVariableListItems(key, context);

            if (items != null && items.size() > 0) {
                addPanel(new ReferenceListPanel(key, items), key);
            }
        }

        updateUserTemplates();

        setDefaultDropDownValue();
    }

    public void updateUserTemplates() {
        ReferenceListFactory builder = ReferenceListFactory.getInstance();

        addPanel(new ReferenceListPanel(ListType.ALL.getValue(), builder.getVariableListItems(ListType.ALL.getValue(), context)), "All");

        ArrayList<CodeTemplate> variableListItems = builder.getVariableListItems(ReferenceListFactory.USER_TEMPLATE_VARIABLES, context);
        if (variableListItems.size() > 0) {
            addPanel(new ReferenceListPanel(ReferenceListFactory.USER_TEMPLATE_VARIABLES, variableListItems), ReferenceListFactory.USER_TEMPLATE_VARIABLES);
        } else if (panels.get(ReferenceListFactory.USER_TEMPLATE_VARIABLES) != null) {
            panels.remove(ReferenceListFactory.USER_TEMPLATE_VARIABLES);
        }

        ArrayList<CodeTemplate> codeListItems = builder.getVariableListItems(ReferenceListFactory.USER_TEMPLATE_CODE, context);
        if (codeListItems.size() > 0) {
            addPanel(new ReferenceListPanel(ReferenceListFactory.USER_TEMPLATE_CODE, codeListItems), ReferenceListFactory.USER_TEMPLATE_CODE);
        } else if (panels.get(ReferenceListFactory.USER_TEMPLATE_CODE) != null) {
            panels.remove(ReferenceListFactory.USER_TEMPLATE_CODE);
        }

        ArrayList<CodeTemplate> functionListItems = builder.getVariableListItems(ReferenceListFactory.USER_TEMPLATE_FUNCTIONS, context);
        if (functionListItems.size() > 0) {
            addPanel(new ReferenceListPanel(ReferenceListFactory.USER_TEMPLATE_FUNCTIONS, functionListItems), ReferenceListFactory.USER_TEMPLATE_FUNCTIONS);
        } else if (panels.get(ReferenceListFactory.USER_TEMPLATE_FUNCTIONS) != null) {
            panels.remove(ReferenceListFactory.USER_TEMPLATE_FUNCTIONS);
        }

        updateDropDown();
    }

    public void setDefaultDropDownValue() {
        variableReferenceDropDownActionPerformed(null);
        variableReferenceDropDown.setSelectedItem(ListType.ALL.getValue());
    }

    public void addPanel(JPanel panel, String name) {
        panels.put(name, panel);
    }

    public void updateDropDown() {
        String[] items = new String[panels.keySet().size()];
        int i = 0;
        for (String s : panels.keySet()) {
            items[i] = s;
            i++;
        }

        Object selectedItem = variableReferenceDropDown.getSelectedItem();

        variableReferenceDropDown.setModel(new DefaultComboBoxModel(items));

        if (selectedItem != null) {
            variableReferenceDropDown.setSelectedItem(selectedItem);
        }
    }

    private void refreshTableList() {
        // only perform filtering if the enable filter is enabled
        if (enableFilter) {
            JPanel panel = panels.get(variableReferenceDropDown.getSelectedItem());
            if (panel instanceof ReferenceListPanel) {
                ReferenceListPanel refListPanel = (ReferenceListPanel) panel;
                refListPanel.refreshTableList(filterField.getText());
            }
            variableScrollPane.setViewportView(panel);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        variableReferenceDropDown = new javax.swing.JComboBox();
        categoryLabel = new javax.swing.JLabel();
        variableScrollPane = new javax.swing.JScrollPane();
        filterLabel = new javax.swing.JLabel();
        filterField = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        variableReferenceDropDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                variableReferenceDropDownActionPerformed(evt);
            }
        });

        categoryLabel.setText("Category:");

        filterLabel.setText("Filter:");

        filterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterFieldKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(categoryLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(variableReferenceDropDown, 0, 70, Short.MAX_VALUE)
                    .addComponent(filterField, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(variableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryLabel)
                    .addComponent(variableReferenceDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterLabel)
                    .addComponent(filterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(variableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filterFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFieldKeyPressed
        refreshTableList();
    }//GEN-LAST:event_filterFieldKeyPressed

    private void filterFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFieldKeyTyped
        refreshTableList();
    }//GEN-LAST:event_filterFieldKeyTyped

    private void filterFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFieldKeyReleased
        refreshTableList();
    }//GEN-LAST:event_filterFieldKeyReleased

    private void variableReferenceDropDownActionPerformed(java.awt.event.ActionEvent evt) {
    	filterField.setText("");  // clear the filter text field
    	refreshTableList();  // refresh table list based on the new filter value
        updateUserTemplates();
        variableScrollPane.setViewportView((panels.get(variableReferenceDropDown.getSelectedItem())));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JTextField filterField;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JComboBox variableReferenceDropDown;
    private javax.swing.JScrollPane variableScrollPane;
    // End of variables declaration//GEN-END:variables
}
