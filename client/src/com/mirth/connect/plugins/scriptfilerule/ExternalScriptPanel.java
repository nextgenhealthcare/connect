/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.scriptfilerule;

import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Rule;

import net.miginfocom.swing.MigLayout;

public class ExternalScriptPanel extends EditorPanel<Rule> {

    public ExternalScriptPanel() {
        initComponents();
        initLayout();
    }

    @Override
    public Rule getDefaults() {
        return new ExternalScriptRule();
    }

    @Override
    public Rule getProperties() {
        ExternalScriptRule props = new ExternalScriptRule();
        props.setScriptPath(pathField.getText().trim());
        return props;
    }

    @Override
    public void setProperties(Rule properties) {
        ExternalScriptRule props = (ExternalScriptRule) properties;
        pathField.setText(props.getScriptPath());
    }

    @Override
    public String checkProperties(Rule properties, boolean highlight) {
        ExternalScriptRule props = (ExternalScriptRule) properties;
        String errors = "";

        if (StringUtils.isBlank(props.getScriptPath())) {
            errors += "The script path cannot be blank.\n";
            if (highlight) {
                pathField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    @Override
    public void resetInvalidProperties() {
        pathField.setBackground(null);
    }

    @Override
    public void setNameActionListener(ActionListener actionListener) {}

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        infoLabel = new JLabel("Enter the path of an external JavaScript file accessible from the Mirth Connect server.");

        pathLabel = new JLabel("Script Path:");
        pathField = new JTextField();
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, gap 12 6"));

        add(infoLabel, "sx");
        add(pathLabel);
        add(pathField, "sx, growx, pushx");
    }

    private JLabel infoLabel;
    private JLabel pathLabel;
    private JTextField pathField;
}