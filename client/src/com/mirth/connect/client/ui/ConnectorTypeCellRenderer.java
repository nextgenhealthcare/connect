/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

public class ConnectorTypeCellRenderer extends DefaultTableCellRenderer {

    private Set<Color> highlightColors = new HashSet<Color>();

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, int row, final int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ConnectorTypeData connectorTypeData = (ConnectorTypeData) value;
        setIcon(null);

        if (connectorTypeData != null) {
            ConnectorTypeDecoration connectorTypeDecoration = connectorTypeData.getDecoration();

            if (connectorTypeDecoration != null) {
                setIcon(connectorTypeDecoration.getIcon());
                setToolTipText(connectorTypeDecoration.getIconToolTipText());

                final Color highlightColor = connectorTypeDecoration.getHighlightColor();

                // If a highlighter doesn't already exist for this color, add one 
                if (highlightColor != null && !highlightColors.contains(highlightColor)) {
                    HighlightPredicate highlighterPredicate = new HighlightPredicate() {
                        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                            if (adapter.column == column) {
                                ConnectorTypeData connectorTypeData = (ConnectorTypeData) table.getValueAt(adapter.row, column);
                                if (connectorTypeData.getDecoration() != null && highlightColor.equals(connectorTypeData.getDecoration().getHighlightColor())) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    };

                    ((JXTable) table).addHighlighter(new ColorHighlighter(highlighterPredicate, highlightColor, Color.BLACK, highlightColor, Color.BLACK));
                    highlightColors.add(highlightColor);
                }
            }
        }

        return component;
    }
}