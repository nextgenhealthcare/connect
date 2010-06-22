package com.mirth.connect.client.ui;

import java.awt.Component;
import java.util.Formatter;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.mirth.connect.client.ui.util.DisplayUtil;

// TODO: Maybe add a type definition to determine if this is a float or an int cell

public class NumberCellRenderer extends DefaultTableCellRenderer {

	public NumberCellRenderer() {
        this(CENTER);
    }
	public NumberCellRenderer(int alignment) {
        super();
        this.setHorizontalAlignment(alignment);
    }
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
                
        setText(DisplayUtil.formatNumber((Integer) value));
        return this;
    }
	
}
