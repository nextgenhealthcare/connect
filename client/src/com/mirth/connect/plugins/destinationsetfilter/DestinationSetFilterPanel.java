/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.destinationsetfilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CenterCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;
import com.mirth.connect.plugins.destinationsetfilter.DestinationSetFilterStep.Behavior;
import com.mirth.connect.plugins.destinationsetfilter.DestinationSetFilterStep.Condition;

public class DestinationSetFilterPanel extends EditorPanel<Step> {

    private ActionListener nameActionListener;

    public DestinationSetFilterPanel() {
        initComponents();
        initLayout();
    }

    @Override
    public Step getDefaults() {
        return new DestinationSetFilterStep();
    }

    @Override
    public Step getProperties() {
        DestinationSetFilterStep props = new DestinationSetFilterStep();

        props.setBehavior((Behavior) behaviorComboBox.getSelectedItem());

        for (int row = 0; row < destinationsTable.getModel().getRowCount(); row++) {
            if ((Boolean) destinationsTable.getModel().getValueAt(row, 0)) {
                props.getMetaDataIds().add((Integer) destinationsTable.getModel().getValueAt(row, 2));
            }
        }

        props.setField(fieldField.getText());
        props.setCondition(getSelectedCondition());
        props.setValues(getValues());

        return props;
    }

    @Override
    public void setProperties(Step properties) {
        DestinationSetFilterStep props = (DestinationSetFilterStep) properties;

        behaviorComboBox.setSelectedItem(props.getBehavior());

        resetDestinationTable();
        for (int row = 0; row < destinationsTable.getModel().getRowCount(); row++) {
            boolean selected = props.getMetaDataIds().contains((Integer) destinationsTable.getModel().getValueAt(row, 2));
            destinationsTable.getModel().setValueAt(selected, row, 0);
        }

        fieldField.setText(props.getField());
        conditionRadioMap.get(props.getCondition()).setSelected(true);
        setValues(props.getValues());
        properties.setName(updateName());
    }

    private void resetDestinationTable() {
        List<Connector> destinations = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel.getDestinationConnectors();
        Object[][] data = new Object[destinations.size()][3];

        int i = 0;
        for (Connector destination : destinations) {
            data[i][0] = false;
            data[i][1] = destination.getName();
            data[i][2] = destination.getMetaDataId();
            i++;
        }

        ((RefreshTableModel) destinationsTable.getModel()).refreshDataVector(data);
    }

    @Override
    public String checkProperties(Step properties, boolean highlight) {
        DestinationSetFilterStep props = (DestinationSetFilterStep) properties;
        String errors = "";

        if (StringUtils.isBlank(props.getField())) {
            errors += "The field cannot be blank.\n";
            if (highlight) {
                fieldField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    @Override
    public void resetInvalidProperties() {
        fieldField.setBackground(null);
    }

    @Override
    public void setNameActionListener(ActionListener actionListener) {
        nameActionListener = actionListener;
    }

    public void setValues(List<String> values) {
        Object[][] tableData = new Object[values.size()][1];
        for (int i = 0; i < values.size(); i++) {
            tableData[i][0] = values.get(i);
        }
        ((RefreshTableModel) valuesTable.getModel()).refreshDataVector(tableData);
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<String>();

        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            if (((String) valuesTable.getValueAt(i, 0)).length() > 0) {
                values.add((String) valuesTable.getValueAt(i, 0));
            }
        }

        return values;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows() {
        valuesTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /** Get the currently selected destination index */
    public int getSelectedRow() {
        if (valuesTable.isEditing()) {
            return valuesTable.getEditingRow();
        } else {
            return valuesTable.getSelectedRow();
        }
    }

    private void setValuesEnabled(boolean enabled) {
        if (valuesTable.isEditing()) {
            valuesTable.getCellEditor().stopCellEditing();
        }
        valuesScrollPane.setEnabled(enabled);
        valuesTable.setEnabled(enabled);
        valuesLabel.setEnabled(enabled);
        newButton.setEnabled(enabled);

        deselectRows();
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        behaviorPanel = new JPanel();
        behaviorPanel.setBackground(getBackground());

        behaviorLabel = new JLabel("Behavior:");
        behaviorRemoveLabel = new JLabel("Remove");

        behaviorComboBox = new JComboBox<Behavior>(Behavior.values());
        behaviorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                behaviorComboBoxActionPerformed();
            }
        });

        behaviorDestinationsLabel = new JLabel(" destinations:");

        selectAllLabel = new JLabel("<html><u>Select All</u></html>");
        selectAllLabel.setForeground(Color.BLUE);
        selectAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                if (((Component) evt.getSource()).isEnabled()) {
                    setDestinationsSelected(true);
                }
            }
        });

        separatorLabel = new JLabel("|");

        deselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        deselectAllLabel.setForeground(Color.BLUE);
        deselectAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deselectAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                if (((Component) evt.getSource()).isEnabled()) {
                    setDestinationsSelected(false);
                }
            }
        });

        destinationsLabel = new JLabel("Destinations:");

        destinationsTable = new MirthTable();
        destinationsTable.setModel(new RefreshTableModel(new String[] { "", "Name", "Id" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        });

        destinationsTable.setRowSelectionAllowed(false);
        destinationsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        destinationsTable.setDragEnabled(false);
        destinationsTable.setOpaque(true);
        destinationsTable.setSortable(false);
        destinationsTable.setFocusable(false);
        destinationsTable.getTableHeader().setReorderingAllowed(false);
        destinationsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            destinationsTable.setHighlighters(highlighter);
        }

        destinationsTable.getColumnExt(0).setMinWidth(18);
        destinationsTable.getColumnExt(0).setMaxWidth(18);
        destinationsTable.getColumnExt(2).setMinWidth(30);
        destinationsTable.getColumnExt(2).setMaxWidth(30);
        destinationsTable.getColumnExt(2).setCellRenderer(new CenterCellRenderer());

        destinationsScrollPane = new JScrollPane(destinationsTable);

        fieldLabel = new JLabel("Field:");

        fieldField = new JTextField();
        fieldField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                documentChanged(evt);
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                documentChanged(evt);
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                documentChanged(evt);
            }

            private void documentChanged(DocumentEvent evt) {
                try {
                    updateName(evt.getDocument().getText(0, evt.getDocument().getLength()));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

        conditionLabel = new JLabel("Condition:");

        conditionRadioMap = new LinkedHashMap<Condition, JRadioButton>();
        ButtonGroup conditionButtonGroup = new ButtonGroup();

        for (Condition condition : Condition.values()) {
            JRadioButton radio = new JRadioButton(condition.toString());
            radio.setBackground(getBackground());
            radio.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    conditionRadioActionPerformed(condition);
                }
            });
            conditionButtonGroup.add(radio);
            conditionRadioMap.put(condition, radio);
        }

        valuesLabel = new JLabel("Values:");

        valuesTable = new MirthTable();
        valuesTable.setModel(new RefreshTableModel(new String[] { "Value" }, 0) {
            boolean[] canEdit = new boolean[] { true };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        valuesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                deleteButton.setEnabled(getSelectedRow() >= 0);
            }
        });

        class RegExTableCellEditor extends TextFieldCellEditor {
            @Override
            public boolean stopCellEditing() {
                deleteButton.setEnabled(true);
                return super.stopCellEditing();
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    deleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                return true;
            }
        }

        valuesTable.getColumnExt(0).setCellEditor(new RegExTableCellEditor());
        valuesTable.setCustomEditorControls(true);

        valuesTable.setSelectionMode(0);
        valuesTable.setRowSelectionAllowed(true);
        valuesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        valuesTable.setDragEnabled(false);
        valuesTable.setOpaque(true);
        valuesTable.setSortable(false);
        valuesTable.getTableHeader().setReorderingAllowed(false);
        valuesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            valuesTable.setHighlighters(highlighter);
        }

        valuesTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateName();
                    }
                });
            }
        });

        valuesScrollPane = new JScrollPane(valuesTable);
        valuesScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                deselectRows();
            }
        });

        newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        deleteButton.setEnabled(false);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill, gap 6"));

        add(behaviorLabel, "right, gapafter 6");

        behaviorPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6"));
        behaviorPanel.add(behaviorRemoveLabel, "split 3");
        behaviorPanel.add(behaviorComboBox);
        behaviorPanel.add(behaviorDestinationsLabel);
        behaviorPanel.add(selectAllLabel, "right, split 3");
        behaviorPanel.add(separatorLabel);
        behaviorPanel.add(deselectAllLabel, "gapafter 3");
        add(behaviorPanel, "growx, sx");

        add(destinationsLabel, "newline, top, right, gapafter 6");
        add(destinationsScrollPane, "grow, sx, h :80");
        add(fieldLabel, "newline, right, gapafter 6");
        add(fieldField, "sx, growx");
        add(conditionLabel, "newline, right, gapafter 6");
        for (JRadioButton radio : conditionRadioMap.values()) {
            add(radio, "split " + conditionRadioMap.size());
        }
        add(valuesLabel, "newline, top, right, gapafter 6");
        add(valuesScrollPane, "grow, push, sy, h :80");
        add(newButton, "top, flowy, split 2, sgx");
        add(deleteButton, "top, sgx");
    }

    private void behaviorComboBoxActionPerformed() {
        boolean removeAll = (Behavior) behaviorComboBox.getSelectedItem() == Behavior.REMOVE_ALL;
        destinationsLabel.setEnabled(!removeAll);
        destinationsTable.setEnabled(!removeAll);
        selectAllLabel.setEnabled(!removeAll);
        separatorLabel.setEnabled(!removeAll);
        deselectAllLabel.setEnabled(!removeAll);
        if (removeAll) {
            setDestinationsSelected(true);
        }
    }

    private void setDestinationsSelected(boolean selected) {
        for (int row = 0; row < destinationsTable.getModel().getRowCount(); row++) {
            destinationsTable.getModel().setValueAt(selected, row, 0);
        }
    }

    private void conditionRadioActionPerformed(Condition condition) {
        setValuesEnabled(condition.isValuesEnabled());
        updateName(condition);
    }

    private Condition getSelectedCondition() {
        for (Entry<Condition, JRadioButton> entry : conditionRadioMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                return entry.getKey();
            }
        }
        return Condition.values()[0];
    }

    private String updateName() {
        return updateName(fieldField.getText(), getSelectedCondition());
    }

    private String updateName(String fieldText) {
        return updateName(fieldText, getSelectedCondition());
    }

    private String updateName(Condition condition) {
        return updateName(fieldField.getText(), condition);
    }

    private String updateName(String fieldText, Condition condition) {
        StringBuilder builder = new StringBuilder("Filter destination(s) if \"").append(fieldText).append("\" ");

        if (condition.isValuesEnabled()) {
            boolean first = true;
            for (String value : getValues()) {
                if (StringUtils.isNotBlank(value)) {
                    if (first) {
                        builder.append(condition.getPresentTense()).append(' ');
                    } else {
                        builder.append(" or ");
                    }
                    builder.append(value);
                    first = false;
                }
            }

            if (first) {
                if (condition == Condition.EQUALS) {
                    builder.append("is blank");
                } else if (condition == Condition.NOT_EQUAL) {
                    builder.append("is not blank");
                } else {
                    builder.append(condition.getPresentTense()).append(" \"\"");
                }
            }
        } else {
            builder.append(condition.getPresentTense());
        }

        String name = builder.toString();

        if (nameActionListener != null) {
            nameActionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name));
        }

        return name;
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        int selectedRow = getSelectedRow();
        if (selectedRow >= 0 && !valuesTable.isEditing()) {
            ((RefreshTableModel) valuesTable.getModel()).removeRow(valuesTable.convertRowIndexToModel(selectedRow));

            if (valuesTable.getRowCount() > 0) {
                if (selectedRow < valuesTable.getRowCount()) {
                    valuesTable.setRowSelectionInterval(selectedRow, selectedRow);
                } else {
                    valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
                }
            }
        }
    }

    private void newButtonActionPerformed(ActionEvent evt) {
        ((RefreshTableModel) valuesTable.getModel()).addRow(new Object[] { "" });
        valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
    }

    private JPanel behaviorPanel;
    private JLabel behaviorLabel;
    private JLabel behaviorRemoveLabel;
    private JComboBox<Behavior> behaviorComboBox;
    private JLabel selectAllLabel;
    private JLabel separatorLabel;
    private JLabel deselectAllLabel;
    private JLabel behaviorDestinationsLabel;
    private JLabel destinationsLabel;
    private MirthTable destinationsTable;
    private JScrollPane destinationsScrollPane;
    private JLabel fieldLabel;
    private JTextField fieldField;
    private JLabel conditionLabel;
    private Map<Condition, JRadioButton> conditionRadioMap;
    private JLabel valuesLabel;
    private MirthTable valuesTable;
    private JScrollPane valuesScrollPane;
    private JButton newButton;
    private JButton deleteButton;
}