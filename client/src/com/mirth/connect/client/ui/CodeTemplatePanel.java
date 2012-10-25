/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.prefs.Preferences;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;

/** The template editor panel. */
public class CodeTemplatePanel extends javax.swing.JPanel {

    private Frame parent;
    public boolean isDeleting = false;
    private int lastModelRow;
    private final String TEMPLATE_NAME_COLUMN_NAME = "Name";
    private final String TEMPLATE_CONTEXT_COLUMN_NAME = "Context";
    private final String TEMPLATE_TYPE_COLUMN_NAME = "Type";
    private final String TEMPLATE_DESCRIPTION_COLUMN_NAME = "Description";
    private final JPanel blankPanel;
    private boolean updating = false;
    private SyntaxDocument jsMappingDoc;
    private final String functionCodeExample = "// modify function_name and parameters as you wish.\n// one function per template is recommended. i.e.) create a new code template for each new function.\nfunction function_name() {\n\t// write code here.\n}";

    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public CodeTemplatePanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastModelRow = -1;
        blankPanel = new JPanel();
        initComponents();
        
        description.setDocument(new MirthFieldConstraints(255));

        ContextType[] contexts = ContextType.values();
        String[] contextNames = new String[contexts.length];

        for (int i = 0; i < contextNames.length; i++) {
            contextNames[i] = contexts[i].getValue();
        }

        context.setModel(new javax.swing.DefaultComboBoxModel(contextNames));

        CodeSnippetType[] types = CodeSnippetType.values();
        String[] typeNames = new String[types.length];

        for (int i = 0; i < typeNames.length; i++) {
            typeNames[i] = types[i].getValue();
        }

        type.setModel(new javax.swing.DefaultComboBoxModel(typeNames));

        makeCodeTemplateTable();

        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());

        template.setDocument(jsMappingDoc);
    }

    /**
     * Makes the codeTemplate table with a parameter that is true if a new
     * codeTemplate should be added as well.
     */
    public void makeCodeTemplateTable() {
        updateCodeTemplateTable();

        templateTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Set the custom cell editor for the CodeTemplate Name column.
        templateTable.getColumnModel().getColumn(templateTable.getColumnModelIndex(TEMPLATE_NAME_COLUMN_NAME)).setCellEditor(new CodeTemplateTableCellEditor());
        templateTable.setCustomEditorControls(true);
        
        templateTable.setSelectionMode(0);
        templateTable.setRowSelectionAllowed(true);
        templateTable.setRowHeight(UIConstants.ROW_HEIGHT);
        templateTable.setSortable(true);
        templateTable.setOpaque(true);
        templateTable.setDragEnabled(false);

        templateTable.getColumnExt(TEMPLATE_CONTEXT_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        templateTable.getColumnExt(TEMPLATE_CONTEXT_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        templateTable.getColumnExt(TEMPLATE_TYPE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        templateTable.getColumnExt(TEMPLATE_TYPE_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        templateTable.getColumnExt(TEMPLATE_DESCRIPTION_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            templateTable.setHighlighters(highlighter);
        }

        // This action is called when a new selection is made on the codeTemplate table.
        templateTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (updating || isDeleting || templateTable.isEditing()) {
                    return;
                }

                if (!evt.getValueIsAdjusting()) {
                    if (lastModelRow != -1
                            && lastModelRow != templateTable.getSelectedModelIndex()
                            && lastModelRow < templateTable.getModel().getRowCount()) {
                        saveCodeTemplate();
                    }

                    loadCodeTemplate();
                    refreshTableRow();
                    checkVisibleTemplateTasks();
                }
            }
        });

        // Mouse listener for trigger-button popup on the table.
        templateTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        templatePane.setViewportView(templateTable);

        // Key Listener trigger for and DEL
        templateTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteCodeTemplate();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    public void updateCodeTemplateTable() {
        Object[][] tableData = null;
        int tableSize = 0;

        if (parent.codeTemplates != null) {
            tableSize = parent.codeTemplates.size();

            tableData = new Object[tableSize][4];
            for (int i = 0; i < tableSize; i++) {
                CodeTemplate codeTemplate = parent.codeTemplates.get(i);
                tableData[i][0] = codeTemplate.getName();
                tableData[i][1] = codeTemplate.getType().getValue();

                for (ContextType c : ContextType.values()) {
                    if (c.getContext() == codeTemplate.getScope()) {
                        tableData[i][2] = c.getValue();
                    }
                }

                tableData[i][3] = codeTemplate.getTooltip();
            }
        }

        if (templateTable != null) {
            RefreshTableModel model = (RefreshTableModel) templateTable.getModel();
            updating = true;
            model.refreshDataVector(tableData);
            updating = false;
        } else {
            templateTable = new MirthTable();
            
            templateTable.setModel(new RefreshTableModel(tableData, new String[]{TEMPLATE_NAME_COLUMN_NAME, TEMPLATE_TYPE_COLUMN_NAME, TEMPLATE_CONTEXT_COLUMN_NAME, TEMPLATE_DESCRIPTION_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{true, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        refreshTableRow();
    }

    private void refreshTableRow() {
        lastModelRow = templateTable.getSelectedModelIndex();
    }

    public void validateCodeTemplate() {
        validateCodeTemplate(template.getText(), true, null);
    }

    public boolean validateCodeTemplate(String script, boolean alertOnSuccess, String name) {
        boolean passed = false;
        StringBuilder sb = new StringBuilder();
        Context context = Context.enter();
        try {
            context.compileString("function rhinoWrapper() {" + script + "\n}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
            sb.append("JavaScript was successfully validated.");
            passed = true;
        } catch (EvaluatorException e) {
            sb.append("Error on line " + e.lineNumber() + ": " + e.getMessage());
            if (name != null) {
                sb.append(" on template named: " + name + ".");
            }
        } catch (Exception e) {
            sb.append("Unknown error occurred during validation.");
        }

        Context.exit();
        if (alertOnSuccess || !alertOnSuccess && !passed) {
            PlatformUI.MIRTH_FRAME.alertInformation(this, sb.toString());
        }
        return passed;
    }

    public void setDefaultCodeTemplate() {
        lastModelRow = -1;

        if (parent.codeTemplates.size() > 0) {
            templateTable.setRowSelectionInterval(0, 0);
        } else {
            deselectCodeTemplateRows();
        }
    }

    /**
     * Checks to see what tasks should be available in the codeTemplate pane
     */
    public void checkVisibleTemplateTasks() {
        int selected = templateTable.getSelectedModelIndex();

        if (selected == UIConstants.ERROR_CONSTANT) {
            parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 5, 6, false);
        } else {
            parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 5, 5, true);
            if (parent.codeTemplates.get(selected).getType() == CodeSnippetType.FUNCTION) {
                parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 6, 6, true);
            } else {
                parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 6, 6, false);
            }
        }
    }

    /** Loads a selected code template and returns true on success. */
    public boolean loadCodeTemplate() {
        int index = templateTable.getSelectedModelIndex();

        if (index == UIConstants.ERROR_CONSTANT) {
            return false;
        }

        boolean enabled = parent.isSaveEnabled();

        CodeTemplate current = parent.codeTemplates.get(index);

        type.setSelectedItem(current.getType().getValue());
        for (ContextType c : ContextType.values()) {
            if (c.getContext() == current.getScope()) {
                context.setSelectedItem(c.getValue());
            }
        }
        description.setText(current.getTooltip());
        template.setText(current.getCode());

        int dividerLocation = split.getDividerLocation();
        split.setRightComponent(bottomPane);
        split.setDividerLocation(dividerLocation);

        parent.setSaveEnabled(enabled);

        return true;
    }

    public boolean saveCodeTemplate() {
        if (lastModelRow == UIConstants.ERROR_CONSTANT) {
            return false;
        }

        int index = lastModelRow;

        boolean enabled = parent.isSaveEnabled();

        CodeTemplate current = parent.codeTemplates.get(index);

        stopCodeTemplateEditing();

        for (CodeSnippetType c : CodeSnippetType.values()) {
            if (c.getValue() == (String) type.getSelectedItem()) {
                current.setType(c);
            }
        }

        for (ContextType c : ContextType.values()) {
            if (c.getValue() == (String) context.getSelectedItem()) {
                current.setScope(c.getContext());
            }
        }

        current.setTooltip(description.getText());
        current.setCode(template.getText());

        parent.setSaveEnabled(enabled);

        return true;
    }

    /**
     * Get the name that should be used for a new codeTemplate so that it is
     * unique.
     */
    private String getNewCodeTemplateName(int size) {
        String temp = "Template ";

        for (int i = 1; i <= size; i++) {
            boolean exists = false;
            for (int j = 0; j < size - 1; j++) {
                if (((String) templateTable.getModel().getValueAt(j, templateTable.getColumnModelIndex(TEMPLATE_NAME_COLUMN_NAME))).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = templateTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                templateTable.setRowSelectionInterval(row, row);
            }
            parent.codeTemplatePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /** Adds a new codeTemplate. */
    public void addCodeTemplate() {
        stopCodeTemplateEditing();
        saveCodeTemplate();

        RefreshTableModel model = (RefreshTableModel) templateTable.getModel();

        CodeTemplate codeTemplate = new CodeTemplate();
        try {
            codeTemplate.setId(parent.mirthClient.getGuid());
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }

        // Set the version when creating new code templates in case they are exported
        // without being retrieved from the server again, which always sets the version
        codeTemplate.setVersion(PlatformUI.SERVER_VERSION);
        codeTemplate.setName(getNewCodeTemplateName(model.getRowCount() + 1));
        codeTemplate.setCode(functionCodeExample);
        codeTemplate.setTooltip("");
        codeTemplate.setScope(ContextType.MESSAGE_CONTEXT.getContext());
        codeTemplate.setType(CodeSnippetType.FUNCTION);

        Object[] rowData = new Object[4];
        rowData[0] = codeTemplate.getName();
        rowData[1] = CodeSnippetType.FUNCTION.getValue();
        rowData[2] = ContextType.MESSAGE_CONTEXT.getValue();
        rowData[3] = codeTemplate.getTooltip();

        parent.codeTemplates.add(codeTemplate);
        model.addRow(rowData);

        int newViewIndex = templateTable.convertRowIndexToView(templateTable.getModel().getRowCount() - 1);
        templateTable.setRowSelectionInterval(newViewIndex, newViewIndex);

        templatePane.getViewport().setViewPosition(new Point(0, templateTable.getRowHeight() * templateTable.getModel().getRowCount()));
        parent.setSaveEnabled(true);
    }

    public void deleteCodeTemplate() {
        if (!parent.alertOption(this, "Are you sure you want to delete this code template?")) {
            return;
        }
        isDeleting = true;

        stopCodeTemplateEditing();

        RefreshTableModel model = (RefreshTableModel) templateTable.getModel();

        int selectedModelIndex = templateTable.getSelectedModelIndex();
        int newViewIndex = templateTable.convertRowIndexToView(selectedModelIndex);
        if (newViewIndex == (model.getRowCount() - 1)) {
            newViewIndex--;
        }

        // must set lastModelRow to -1 so that when setting the new
        // row selection below the old data won't try to be saved.
        lastModelRow = -1;
        parent.codeTemplates.remove(selectedModelIndex);
        model.removeRow(selectedModelIndex);

        parent.setSaveEnabled(true);

        isDeleting = false;

        if (parent.codeTemplates.size() == 0) {
            deselectCodeTemplateRows();
        } else {
            templateTable.setRowSelectionInterval(newViewIndex, newViewIndex);
        }
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectCodeTemplateRows() {
        templateTable.clearSelection();
        parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 5, 6, false);
        resetBlankPane();
    }

    public void resetBlankPane() {
        int dividerLocation = split.getDividerLocation();
        split.setRightComponent(blankPanel);
        split.setDividerLocation(dividerLocation);
    }

    public void stopCodeTemplateEditing() {
        if (templateTable.isEditing()) {
            templateTable.getColumnModel().getColumn(templateTable.getColumnModelIndex(TEMPLATE_NAME_COLUMN_NAME)).getCellEditor().stopCellEditing();
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        split = new javax.swing.JSplitPane();
        templatePane = new javax.swing.JScrollPane();
        templateTable = null;
        bottomPane = new javax.swing.JPanel();
        template = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,true,ContextType.MESSAGE_CONTEXT.getContext());
        templateLabel = new javax.swing.JLabel();
        contextLabel = new javax.swing.JLabel();
        context = new com.mirth.connect.client.ui.components.MirthComboBox();
        descriptionLabel = new javax.swing.JLabel();
        description = new com.mirth.connect.client.ui.components.MirthTextField();
        typeLabel = new javax.swing.JLabel();
        type = new com.mirth.connect.client.ui.components.MirthComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setDividerLocation(125);
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        templatePane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        templatePane.setViewportView(templateTable);

        split.setLeftComponent(templatePane);

        bottomPane.setBackground(new java.awt.Color(255, 255, 255));

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        templateLabel.setText("Template:");

        contextLabel.setText("Context:");

        context.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        context.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextActionPerformed(evt);
            }
        });

        descriptionLabel.setText("Description:");

        description.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                descriptionKeyReleased(evt);
            }
        });

        typeLabel.setText("Type:");

        type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPaneLayout = new javax.swing.GroupLayout(bottomPane);
        bottomPane.setLayout(bottomPaneLayout);
        bottomPaneLayout.setHorizontalGroup(
            bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(typeLabel)
                    .addComponent(descriptionLabel)
                    .addComponent(contextLabel)
                    .addComponent(templateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addComponent(context, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(description, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        bottomPaneLayout.setVerticalGroup(
            bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextLabel)
                    .addComponent(context, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionLabel)
                    .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateLabel)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                .addContainerGap())
        );

        split.setRightComponent(bottomPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void typeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_typeActionPerformed
    {//GEN-HEADEREND:event_typeActionPerformed
        if (isDeleting) {
            return;
        }

        updating = true;
        if (((String) type.getSelectedItem()).equals(CodeSnippetType.FUNCTION.getValue())) {
            templateLabel.setText("Function:");
            parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 6, 6, true);
        } else {
            templateLabel.setText("Template:");
            parent.setVisibleTasks(parent.codeTemplateTasks, parent.codeTemplatePopupMenu, 6, 6, false);
        }

        if (templateTable.getSelectedModelIndex() != UIConstants.ERROR_CONSTANT) {
            templateTable.getModel().setValueAt(type.getSelectedItem(), templateTable.getSelectedModelIndex(), templateTable.getColumnModelIndex(TEMPLATE_TYPE_COLUMN_NAME));
        }
        updating = false;
    }//GEN-LAST:event_typeActionPerformed

    private void contextActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_contextActionPerformed
    {//GEN-HEADEREND:event_contextActionPerformed
        if (isDeleting) {
            return;
        }

        updating = true;
        if (templateTable.getSelectedModelIndex() != UIConstants.ERROR_CONSTANT) {
            templateTable.getModel().setValueAt(context.getSelectedItem(), templateTable.getSelectedModelIndex(), templateTable.getColumnModelIndex(TEMPLATE_CONTEXT_COLUMN_NAME));
        }
        updating = false;
    }//GEN-LAST:event_contextActionPerformed

    private void descriptionKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_descriptionKeyReleased
    {//GEN-HEADEREND:event_descriptionKeyReleased
        if (isDeleting) {
            return;
        }

        updating = true;
        if (templateTable.getSelectedModelIndex() != UIConstants.ERROR_CONSTANT) {
            templateTable.getModel().setValueAt(description.getText(), templateTable.getSelectedModelIndex(), templateTable.getColumnModelIndex(TEMPLATE_DESCRIPTION_COLUMN_NAME));
        }
        updating = false;
    }//GEN-LAST:event_descriptionKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPane;
    private com.mirth.connect.client.ui.components.MirthComboBox context;
    private javax.swing.JLabel contextLabel;
    private com.mirth.connect.client.ui.components.MirthTextField description;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JSplitPane split;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea template;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JScrollPane templatePane;
    private com.mirth.connect.client.ui.components.MirthTable templateTable;
    private com.mirth.connect.client.ui.components.MirthComboBox type;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
