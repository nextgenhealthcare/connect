/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.javascriptstep;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.util.JavaScriptSharedUtil;

import net.miginfocom.swing.MigLayout;

public class JavaScriptPanel extends EditorPanel<Step> {

    public JavaScriptPanel() {
        initComponents();
        initLayout();
    }

    @Override
    public Step getDefaults() {
        return new JavaScriptStep();
    }

    @Override
    public Step getProperties() {
        JavaScriptStep props = new JavaScriptStep();

        props.setScript(scriptTextArea.getText().trim());

        return props;
    }

    @Override
    public void setProperties(Step properties) {
        JavaScriptStep props = (JavaScriptStep) properties;

        scriptTextArea.setText(props.getScript());
    }

    @Override
    public String checkProperties(Step properties, boolean highlight) {
        JavaScriptStep props = (JavaScriptStep) properties;
        try {
            Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
            context.compileString("function rhinoWrapper() {" + props.getScript() + "\n}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
        } catch (EvaluatorException e) {
            return "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
        } catch (Exception e) {
            return "Unknown error occurred during validation.";
        } finally {
            Context.exit();
        }
        return null;
    }

    @Override
    public void resetInvalidProperties() {}

    @Override
    public void setNameActionListener(ActionListener actionListener) {}

    public void setContextType(ContextType contextType) {
        scriptTextArea.setContextType(contextType);
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        scriptTextArea = new MirthRTextScrollPane(null, true);
        scriptTextArea.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3"));

        add(scriptTextArea, "grow, push");
    }

    private MirthRTextScrollPane scriptTextArea;
}