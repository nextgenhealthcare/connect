/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.xsltstep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Step;

import net.miginfocom.swing.MigLayout;

public class XsltStepPanel extends EditorPanel<Step> {

    public XsltStepPanel() {
        initComponents();
        initToolTips();
        initLayout();
    }

    @Override
    public Step getDefaults() {
        return new XsltStep();
    }

    @Override
    public Step getProperties() {
        XsltStep props = new XsltStep();
        props.setSourceXml(sourceXMLField.getText().trim());
        props.setResultVariable(resultField.getText().trim());
        props.setUseCustomFactory(transformerFactoryCustomRadio.isSelected());
        props.setCustomFactory(transformerFactoryCustomField.getText().trim());
        props.setTemplate(xsltTemplateTextArea.getText());
        return props;
    }

    @Override
    public void setProperties(Step properties) {
        XsltStep props = (XsltStep) properties;

        sourceXMLField.setText(props.getSourceXml());
        resultField.setText(props.getResultVariable());

        if (props.isUseCustomFactory()) {
            transformerFactoryCustomRadio.setSelected(true);
            customRadioActionPerformed();
        } else {
            transformerFactoryDefaultRadio.setSelected(true);
            defaultRadioActionPerformed();
        }

        transformerFactoryCustomField.setText(props.getCustomFactory());

        xsltTemplateTextArea.setText(props.getTemplate());
    }

    @Override
    public String checkProperties(Step properties, boolean highlight) {
        XsltStep props = (XsltStep) properties;
        String errors = "";

        if (StringUtils.isBlank(props.getSourceXml())) {
            errors += "The source XML string cannot be blank.\n";
            if (highlight) {
                sourceXMLField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (StringUtils.isBlank(props.getResultVariable())) {
            errors += "The result variable cannot be blank.\n";
            if (highlight) {
                resultField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    @Override
    public void resetInvalidProperties() {
        sourceXMLField.setBackground(null);
        resultField.setBackground(null);
    }

    @Override
    public void addNameActionListener(ActionListener actionListener) {}

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        sourceXMLLabel = new JLabel("Source XML String:");
        sourceXMLField = new JTextField();

        resultLabel = new JLabel("Result:");
        resultField = new JTextField();

        transformerFactoryLabel = new JLabel("Transformer Factory:");
        ButtonGroup factoryButtonGroup = new ButtonGroup();

        transformerFactoryDefaultRadio = new MirthRadioButton("Default");
        transformerFactoryDefaultRadio.setBackground(getBackground());
        transformerFactoryDefaultRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                defaultRadioActionPerformed();
            }
        });
        factoryButtonGroup.add(transformerFactoryDefaultRadio);

        transformerFactoryCustomRadio = new MirthRadioButton("Custom");
        transformerFactoryCustomRadio.setBackground(getBackground());
        transformerFactoryCustomRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                customRadioActionPerformed();
            }
        });
        factoryButtonGroup.add(transformerFactoryCustomRadio);

        transformerFactoryCustomField = new MirthTextField();

        xsltTemplateLabel = new JLabel("XSLT Template:");

        xsltTemplateTextArea = new MirthRTextScrollPane(null, true, SyntaxConstants.SYNTAX_STYLE_XML, false);
        xsltTemplateTextArea.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initToolTips() {
        String toolTipText = "<html>Select default to use the platform default Tra4nsformer Factory.<br/>Select custom to provide a custom Transformer Factory class.</html>";
        transformerFactoryDefaultRadio.setToolTipText(toolTipText);
        transformerFactoryCustomRadio.setToolTipText(toolTipText);

        transformerFactoryCustomField.setToolTipText("The fully-qualified class name of the custom Transformer Factory.");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, gap 12 6"));

        add(sourceXMLLabel, "right");
        add(sourceXMLField, "growx");
        add(resultLabel, "newline, right");
        add(resultField, "growx");
        add(transformerFactoryLabel, "newline, right");
        add(transformerFactoryDefaultRadio, "split 3");
        add(transformerFactoryCustomRadio);
        add(transformerFactoryCustomField, "growx");
        add(xsltTemplateLabel, "newline, right, top");
        add(xsltTemplateTextArea, "grow, push");
    }

    private void defaultRadioActionPerformed() {
        transformerFactoryCustomField.setEnabled(false);
    }

    private void customRadioActionPerformed() {
        transformerFactoryCustomField.setEnabled(true);
    }

    private JLabel sourceXMLLabel;
    private JTextField sourceXMLField;
    private JLabel resultLabel;
    private JTextField resultField;
    private JLabel transformerFactoryLabel;
    private JRadioButton transformerFactoryDefaultRadio;
    private JRadioButton transformerFactoryCustomRadio;
    private JTextField transformerFactoryCustomField;
    private JLabel xsltTemplateLabel;
    private MirthRTextScrollPane xsltTemplateTextArea;
}