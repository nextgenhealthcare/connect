/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.JavaScriptSharedUtil.ExprPart;

public class IteratorWizardDialog<C extends FilterTransformerElement> extends MirthDialog {

    private static final Color GREEN = new Color(0, 169, 0);

    private String targetSuffix;
    private List<ExprPart> targetComponents;
    private String indexVariable;
    private List<String> ancestorIndexVariables;
    private String outbound;
    private List<ExprPart> outboundComponents;
    private boolean accepted;

    public IteratorWizardDialog(String target, String indexVariable, List<String> ancestorIndexVariables, String outbound) {
        super(PlatformUI.MIRTH_FRAME, "Iterator Wizard", true);
        this.indexVariable = indexVariable;
        this.ancestorIndexVariables = ancestorIndexVariables;
        this.outbound = outbound;

        if (StringUtils.endsWith(target, ".toString()")) {
            targetSuffix = ".toString()";
            target = StringUtils.removeEnd(target, targetSuffix);
        }
        targetComponents = JavaScriptSharedUtil.getExpressionParts(target, false);

        if (outbound != null) {
            outboundComponents = JavaScriptSharedUtil.getExpressionParts(outbound, false);
        }

        initComponents();
        initLayout();

        // Find the first index after all ancestor Iterator index variables are located
        int minIndex = 0;
        if (!ancestorIndexVariables.isEmpty()) {
            minIndex = targetComponents.size();
            for (int i = targetComponents.size() - 1; i >= 0; i--) {
                if (ancestorIndexVariables.contains(targetComponents.get(i).getPropertyName())) {
                    break;
                }
                minIndex = i;
            }
        }

        // Find the index to select in the target, starting at the minIndex and preferring something > 0
        int index = -1;
        for (int i = minIndex; i < targetComponentRadioButtons.size(); i++) {
            if (targetComponentRadioButtons.get(i).isVisible()) {
                index = i;
                if (index > 0) {
                    break;
                }
            }
        }
        // If nothing was found, find an alternative index
        if (index < 0) {
            for (int i = 0; i < minIndex; i++) {
                if (targetComponentRadioButtons.get(i).isVisible()) {
                    index = i;
                    if (index > 0) {
                        break;
                    }
                }
            }
        }

        // Select the target radio button
        if (index >= 0) {
            targetComponentRadioButtons.get(index).setSelected(true);
            targetComponentRadioButtonActionPerformed(index);
        }

        // Make sure that at least one outbound radio button is selected
        if (outbound != null && (index >= outboundComponents.size() || !outboundComponentRadioButtons.get(index).isVisible())) {
            for (int i = 0; i < outboundComponentRadioButtons.size(); i++) {
                if (outboundComponentRadioButtons.get(i).isVisible()) {
                    outboundComponentRadioButtons.get(i).setSelected(true);
                    outboundComponentRadioButtonActionPerformed(i);
                    break;
                }
            }
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(450, outbound != null ? 289 : 199));
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        okButton.requestFocus();
        setVisible(true);
    }

    public boolean wasAccepted() {
        return accepted;
    }

    public void fillIteratorProperties(IteratorProperties<C> props) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < targetComponentRadioButtons.size(); i++) {
            builder.append(targetComponents.get(i).getValue());
            if (targetComponentRadioButtons.get(i).isSelected()) {
                break;
            }
        }
        props.setTarget(builder.toString());
        props.setIndexVariable(indexVariable);
        props.getPrefixSubstitutions().add(props.getTarget());

        if (outbound != null) {
            builder = new StringBuilder();
            for (int i = 0; i < outboundComponentRadioButtons.size(); i++) {
                builder.append(outboundComponents.get(i).getValue());
                if (outboundComponentRadioButtons.get(i).isSelected()) {
                    break;
                }
            }
            props.getPrefixSubstitutions().add(builder.toString());
        }
    }

    private void initComponents() {
        setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        getContentPane().setBackground(getBackground());

        targetLabel = new JLabel("Select the part of the object to iterate on:");

        targetComponentLabels = new ArrayList<JLabel>();
        targetComponentRadioButtons = new ArrayList<JRadioButton>();
        ButtonGroup targetButtonGroup = new ButtonGroup();

        for (int i = 0; i < targetComponents.size(); i++) {
            final int index = i;
            ExprPart component = targetComponents.get(index);

            JLabel label = new JLabel(component.getValue());
            targetComponentLabels.add(label);

            JRadioButton radioButton = new JRadioButton();
            radioButton.setBackground(getBackground());
            radioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    targetComponentRadioButtonActionPerformed(index);
                }
            });

            if (i + 1 < targetComponents.size() && ancestorIndexVariables.contains(targetComponents.get(i + 1).getPropertyName())) {
                radioButton.setVisible(false);
                radioButton.setEnabled(false);
            } else {
                targetButtonGroup.add(radioButton);
            }

            targetComponentRadioButtons.add(radioButton);
        }

        targetPanel = new JPanel();
        targetPanel.setBackground(getBackground());

        targetScrollPane = new JScrollPane(targetPanel);
        targetScrollPane.setBorder(BorderFactory.createEmptyBorder());

        if (outbound != null) {
            outboundLabel = new JLabel("Select the part of the outbound message to iterate through:");

            outboundComponentLabels = new ArrayList<JLabel>();
            outboundComponentRadioButtons = new ArrayList<JRadioButton>();
            ButtonGroup outboundButtonGroup = new ButtonGroup();

            for (int i = 0; i < outboundComponents.size(); i++) {
                final int index = i;
                ExprPart component = outboundComponents.get(index);

                JLabel label = new JLabel(component.getValue());
                outboundComponentLabels.add(label);

                JRadioButton radioButton = new JRadioButton();
                radioButton.setBackground(getBackground());
                radioButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        outboundComponentRadioButtonActionPerformed(index);
                    }
                });

                if (i + 1 < outboundComponents.size() && ancestorIndexVariables.contains(outboundComponents.get(i + 1).getPropertyName())) {
                    radioButton.setVisible(false);
                    radioButton.setEnabled(false);
                } else {
                    outboundButtonGroup.add(radioButton);
                }

                outboundComponentRadioButtons.add(radioButton);
            }

            outboundPanel = new JPanel();
            outboundPanel.setBackground(getBackground());

            outboundScrollPane = new JScrollPane(outboundPanel);
            outboundScrollPane.setBorder(BorderFactory.createEmptyBorder());
        }

        substitutionsLabel = new JLabel("The following drag-and-drop substitutions will be made:");

        substitutionsTargetLabel1 = new JLabel();
        substitutionsTargetLabel2 = new JLabel(" -> ");
        substitutionsTargetLabel3 = new JLabel();
        substitutionsTargetIndexLabel = new JLabel("<html><b>[" + StringEscapeUtils.escapeHtml4(indexVariable) + "]</b></html>");
        substitutionsTargetIndexLabel.setForeground(GREEN);
        substitutionsTargetLabel4 = new JLabel();

        if (outbound != null) {
            substitutionsOutboundLabel1 = new JLabel();
            substitutionsOutboundLabel2 = new JLabel(" -> ");
            substitutionsOutboundLabel3 = new JLabel();
            substitutionsOutboundIndexLabel = new JLabel("<html><b>[" + StringEscapeUtils.escapeHtml4(indexVariable) + "]</b></html>");
            substitutionsOutboundIndexLabel.setForeground(GREEN);
            substitutionsOutboundLabel4 = new JLabel();
        }

        substitutionsPanel = new JPanel();
        substitutionsPanel.setBackground(getBackground());

        substitutionsScrollPane = new JScrollPane(substitutionsPanel);
        substitutionsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        separator = new JSeparator(SwingConstants.HORIZONTAL);

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                accepted = true;
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, gapx 0"));

        add(targetLabel, "sx");

        targetPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 0, gap 0"));
        for (JLabel label : targetComponentLabels) {
            targetPanel.add(label, "align center");
        }
        for (int i = 0; i < targetComponentRadioButtons.size(); i++) {
            targetPanel.add(targetComponentRadioButtons.get(i), (i == 0 ? "newline, " : "") + "align center");
        }
        add(targetScrollPane, "newline, gapleft 30, growx, h 32:50, sx");

        if (outbound != null) {
            add(outboundLabel, "newline, sx");

            outboundPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 0, gap 0"));
            for (JLabel label : outboundComponentLabels) {
                outboundPanel.add(label, "align center");
            }
            for (int i = 0; i < outboundComponentRadioButtons.size(); i++) {
                outboundPanel.add(outboundComponentRadioButtons.get(i), (i == 0 ? "newline, " : "") + "align center");
            }
            add(outboundScrollPane, "newline, gapleft 30, growx, h 32:50, sx");
        }

        add(substitutionsLabel, "newline, sx");

        substitutionsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 0, gap 0"));

        substitutionsPanel.add(substitutionsTargetLabel1);
        substitutionsPanel.add(substitutionsTargetLabel2);
        substitutionsPanel.add(substitutionsTargetLabel3, "split 3, gapright 0");
        substitutionsPanel.add(substitutionsTargetIndexLabel, "gapleft 0, gapright 0");
        substitutionsPanel.add(substitutionsTargetLabel4, "gapleft 0");

        if (outbound != null) {
            substitutionsPanel.add(substitutionsOutboundLabel1, "newline");
            substitutionsPanel.add(substitutionsOutboundLabel2);
            substitutionsPanel.add(substitutionsOutboundLabel3, "split 3, gapright 0");
            substitutionsPanel.add(substitutionsOutboundIndexLabel, "gapleft 0, gapright 0");
            substitutionsPanel.add(substitutionsOutboundLabel4, "gapleft 0");
        }

        add(substitutionsScrollPane, "newline, sx, growx, h " + (outbound != null ? "32:50" : "14:32") + ", gapleft 30");

        add(separator, "newline, growx, sx, pushx");

        add(okButton, "newline, right, sx, split 2, w 45!");
        add(cancelButton, "w 45!");
    }

    private void targetComponentRadioButtonActionPerformed(int index) {
        StringBuilder builder1 = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        StringBuilder builder3 = new StringBuilder();
        substitutionsTargetIndexLabel.setVisible(false);

        for (int i = 0; i < targetComponents.size(); i++) {
            ExprPart component = targetComponents.get(i);
            JLabel label = targetComponentLabels.get(i);

            if (i <= index) {
                label.setText("<html><b>" + StringEscapeUtils.escapeHtml4(component.getValue()) + "</b></html>");
                label.setForeground(GREEN);
            } else {
                label.setText(component.getValue());
                label.setForeground(Color.BLACK);
            }

            builder1.append(component.getValue());
            if (i <= index) {
                builder2.append(component.getValue());
                if (i == index) {
                    substitutionsTargetIndexLabel.setVisible(true);
                }
            } else {
                builder3.append(component.getValue());
            }
        }

        substitutionsTargetLabel1.setText(builder1.toString());
        substitutionsTargetLabel3.setText(builder2.toString());
        substitutionsTargetLabel4.setText(builder3.toString());

        if (outbound != null && index < outboundComponents.size() && outboundComponentRadioButtons.get(index).isVisible()) {
            outboundComponentRadioButtons.get(index).setSelected(true);
            outboundComponentRadioButtonActionPerformed(index);
        }
    }

    private void outboundComponentRadioButtonActionPerformed(int index) {
        StringBuilder builder1 = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        StringBuilder builder3 = new StringBuilder();
        substitutionsOutboundIndexLabel.setVisible(false);

        for (int i = 0; i < outboundComponents.size(); i++) {
            ExprPart component = outboundComponents.get(i);
            JLabel label = outboundComponentLabels.get(i);

            if (i <= index) {
                label.setText("<html><b>" + StringEscapeUtils.escapeHtml4(component.getValue()) + "</b></html>");
                label.setForeground(GREEN);
            } else {
                label.setText(component.getValue());
                label.setForeground(Color.BLACK);
            }

            builder1.append(component.getValue());
            if (i <= index) {
                builder2.append(component.getValue());
                if (i == index) {
                    substitutionsOutboundIndexLabel.setVisible(true);
                }
            } else {
                builder3.append(component.getValue());
            }
        }

        substitutionsOutboundLabel1.setText(builder1.toString());
        substitutionsOutboundLabel3.setText(builder2.toString());
        substitutionsOutboundLabel4.setText(builder3.toString());
    }

    private JLabel targetLabel;
    private List<JLabel> targetComponentLabels;
    private List<JRadioButton> targetComponentRadioButtons;
    private JPanel targetPanel;
    private JScrollPane targetScrollPane;

    private JLabel outboundLabel;
    private List<JLabel> outboundComponentLabels;
    private List<JRadioButton> outboundComponentRadioButtons;
    private JPanel outboundPanel;
    private JScrollPane outboundScrollPane;

    private JLabel substitutionsLabel;
    private JLabel substitutionsTargetLabel1;
    private JLabel substitutionsTargetLabel2;
    private JLabel substitutionsTargetLabel3;
    private JLabel substitutionsTargetIndexLabel;
    private JLabel substitutionsTargetLabel4;
    private JLabel substitutionsOutboundLabel1;
    private JLabel substitutionsOutboundLabel2;
    private JLabel substitutionsOutboundLabel3;
    private JLabel substitutionsOutboundIndexLabel;
    private JLabel substitutionsOutboundLabel4;
    private JPanel substitutionsPanel;
    private JScrollPane substitutionsScrollPane;

    private JSeparator separator;
    private JButton okButton;
    private JButton cancelButton;
}