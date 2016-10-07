/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.util.JavaScriptContextUtil;

public class JavaScriptReader extends ConnectorSettingsPanel {

    public JavaScriptReader() {
        initComponents();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new JavaScriptReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        JavaScriptReceiverProperties properties = new JavaScriptReceiverProperties();

        properties.setScript(javascriptTextPane.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        JavaScriptReceiverProperties props = (JavaScriptReceiverProperties) properties;

        javascriptTextPane.setText(props.getScript());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new JavaScriptReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        JavaScriptReceiverProperties props = (JavaScriptReceiverProperties) properties;

        boolean valid = true;

        if (props.getScript().length() == 0) {
            valid = false;
            if (highlight) {
                javascriptTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    public TransferMode getTransferMode() {
        return TransferMode.JAVASCRIPT;
    }

    @Override
    public void resetInvalidProperties() {
        javascriptTextPane.setBackground(null);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        javascriptTextPane.updateDisplayOptions();
    }

    @Override
    public String doValidate(ConnectorProperties properties, boolean highlight) {
        JavaScriptReceiverProperties props = (JavaScriptReceiverProperties) properties;

        String error = null;

        String script = props.getScript();

        if (script.length() != 0) {
            Context context = JavaScriptContextUtil.getGlobalContextForValidation();
            try {
                context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
            } catch (EvaluatorException e) {
                if (error == null) {
                    error = "";
                }
                error += "Error in connector \"" + getName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
            } catch (Exception e) {
                if (error == null) {
                    error = "";
                }
                error += "Error in connector \"" + getName() + "\" at Javascript:\nUnknown error occurred during validation.";
            }

            Context.exit();
        }

        return error;
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        jsLabel = new JLabel("JavaScript:");
        javascriptTextPane = new MirthRTextScrollPane(ContextType.SOURCE_RECEIVER, true);
        javascriptTextPane.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6 6", "6[]13[]"));
        add(jsLabel, "top, right");
        add(javascriptTextPane, "grow, push, w :400, h :100");
    }

    private JLabel jsLabel;
    private MirthRTextScrollPane javascriptTextPane;
}
