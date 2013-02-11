package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.client.ui.editors.JavaScriptEditorDialog;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class DataTypePropertiesCellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final String DIFFERENT_VALUES = "<Different Values>";
	private JTextField textField;
    private MirthTriStateCheckBox checkBox;
    private JLabel label;
    private JButton button;
    private JPanel panel;
    private PropertyEditorType valueType;
    
    public DataTypePropertiesCellEditor() {
        textField = new JTextField();
        
        checkBox = new MirthTriStateCheckBox();
        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Fire editing stopped each time the checkbox is clicked,
                // Otherwise the value will not be saved until it loses focus
                fireEditingStopped();
            }
            
        });
        
        label = new JLabel();
        
        button = new JButton("Edit");
        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
        button.addActionListener(new JavaScriptActionListener());
        
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        panel.add(label, constraints);
        panel.add(button);
    }
    
    @Override
    public Object getCellEditorValue() {
        if (valueType == PropertyEditorType.BOOLEAN) {
        	if (checkBox.getState() == MirthTriStateCheckBox.CHECKED) {
        		return true;
        	} else if (checkBox.getState() == MirthTriStateCheckBox.UNCHECKED) {
        		return false;
        	}
        } else if (valueType == PropertyEditorType.STRING) {
            return textField.getText();
        } else if (valueType == PropertyEditorType.JAVASCRIPT) {
            return label.getText();
        }
        
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof DataTypeNodeDescriptor) {
            DataTypeNodeDescriptor nodeDescriptor = (DataTypeNodeDescriptor) value;
            valueType = nodeDescriptor.getEditorType();
            
            if (nodeDescriptor.getEditorType() == PropertyEditorType.BOOLEAN) {
                checkBox.setBackground(table.getSelectionBackground());
                
                Boolean booleanValue = (Boolean) nodeDescriptor.getValue();
                if (nodeDescriptor.isMultipleValues()) {
            		checkBox.setText(DIFFERENT_VALUES);
            		checkBox.setState(MirthTriStateCheckBox.PARTIAL);
            	} else if (booleanValue) {
            		checkBox.setText("");
            		checkBox.setState(MirthTriStateCheckBox.CHECKED);
            	} else {
            		checkBox.setText("");
            		checkBox.setState(MirthTriStateCheckBox.UNCHECKED);
            	}
                return checkBox;
            } else if (nodeDescriptor.getEditorType() == PropertyEditorType.STRING) {
                textField.setText((String) nodeDescriptor.getValue());
                
                // This forces the textField to request focus if a keypress started the edit.
                SwingUtilities.invokeLater(new Runnable() {  
                    public void run() {  
                        textField.requestFocus();  
                    }  
                }); 
            } else if (nodeDescriptor.getEditorType() == PropertyEditorType.JAVASCRIPT) {
                label.setText((String) nodeDescriptor.getValue());
                panel.setBackground(table.getSelectionBackground());
                return panel;
            }
        }
        
        return textField;
    }
    
    private class JavaScriptActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Bring up the Javascript editor dialog 
            JavaScriptEditorDialog dialog = new JavaScriptEditorDialog(label.getText());

            // Update the JLabel with the script from the editor
            label.setText(dialog.getSavedScript());
            
            // Fire editing stopped to save the value immediately
            fireEditingStopped();
        }
        
    }

}
