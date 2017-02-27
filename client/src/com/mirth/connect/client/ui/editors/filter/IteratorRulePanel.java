/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.client.ui.editors.IteratorPanel;
import com.mirth.connect.model.IteratorElement;
import com.mirth.connect.model.IteratorRule;
import com.mirth.connect.model.Rule;

public class IteratorRulePanel extends IteratorPanel<Rule> {

    @Override
    public Rule getDefaults() {
        IteratorRule rule = new IteratorRule();
        rule.setName(getName("...", rule.getProperties().isIntersectIterations() ? AcceptMessageValue.ALL : AcceptMessageValue.AT_LEAST_ONE));
        return rule;
    }

    @Override
    protected IteratorElement<Rule> newIteratorElement() {
        return new IteratorRule();
    }

    @Override
    public Rule getProperties() {
        IteratorRule props = (IteratorRule) super.getProperties();

        props.getProperties().setIntersectIterations((AcceptMessageValue) acceptMessageComboBox.getSelectedItem() == AcceptMessageValue.ALL);
        props.getProperties().setBreakEarly(breakEarlyYesRadio.isSelected());

        return props;
    }

    @Override
    public void setProperties(Rule properties) {
        super.setProperties(properties);
        IteratorRule props = (IteratorRule) properties;

        if (props.getProperties().isIntersectIterations()) {
            acceptMessageComboBox.setSelectedItem(AcceptMessageValue.ALL);
        } else {
            acceptMessageComboBox.setSelectedItem(AcceptMessageValue.AT_LEAST_ONE);
        }

        if (props.getProperties().isBreakEarly()) {
            breakEarlyYesRadio.setSelected(true);
        } else {
            breakEarlyNoRadio.setSelected(true);
        }
    }

    @Override
    protected String getName(String target) {
        return getName(target, (AcceptMessageValue) acceptMessageComboBox.getSelectedItem());
    }

    @Override
    public void setName(IteratorElement<Rule> properties) {
        IteratorRule props = (IteratorRule) properties;
        props.setName(getName(props.getProperties().getTarget(), props.getProperties().isIntersectIterations() ? AcceptMessageValue.ALL : AcceptMessageValue.AT_LEAST_ONE));
    }

    private String getName(String target, AcceptMessageValue acceptMessageValue) {
        StringBuilder name = new StringBuilder();
        name.append("Accept message if ").append(acceptMessageValue.toString().toLowerCase()).append(" of the iterations return");
        if (acceptMessageValue == AcceptMessageValue.AT_LEAST_ONE) {
            name.append('s');
        }
        name.append(" true for each ").append(target);
        return name.toString();
    }

    @Override
    protected void initComponents() {
        acceptMessageLabel = new JLabel("Accept Message If:");

        acceptMessageComboBox = new JComboBox<AcceptMessageValue>(AcceptMessageValue.values());
        acceptMessageComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (acceptMessageComboBox.getSelectedItem() == AcceptMessageValue.ALL) {
                    acceptMessageLabel2.setText("<html>of the iterations return <b>true</b></html>");
                } else {
                    acceptMessageLabel2.setText("<html>of the iterations returns <b>true</b></html>");
                }
                updateName();
            }
        });

        acceptMessageLabel2 = new JLabel("<html>of the iterations returns <b>true</b></html>");

        breakEarlyLabel = new JLabel("Break Early:");
        ButtonGroup breakEarlyButtonGroup = new ButtonGroup();
        String toolTipText = "<html>If this is enabled, the iterator loop will terminate<br/>as quickly as possible. For example if \"At Least One\"<br/>is chosen above, the loop will terminate as soon as<br/>the first iteration returns <b>true</b>.</html>";

        breakEarlyYesRadio = new JRadioButton("Yes");
        breakEarlyYesRadio.setBackground(getBackground());
        breakEarlyYesRadio.setToolTipText(toolTipText);
        breakEarlyButtonGroup.add(breakEarlyYesRadio);

        breakEarlyNoRadio = new JRadioButton("No");
        breakEarlyNoRadio.setBackground(getBackground());
        breakEarlyNoRadio.setToolTipText(toolTipText);
        breakEarlyButtonGroup.add(breakEarlyNoRadio);
    }

    @Override
    protected void addMiddleComponents() {
        add(acceptMessageLabel, "newline, right, gapafter 6");
        add(acceptMessageComboBox, "split 2");
        add(acceptMessageLabel2);
        add(breakEarlyLabel, "newline, right, gapafter 6");
        add(breakEarlyYesRadio, "split 2");
        add(breakEarlyNoRadio);
    }

    private enum AcceptMessageValue {
        AT_LEAST_ONE, ALL;

        @Override
        public String toString() {
            return WordUtils.capitalizeFully(super.toString().replace('_', ' '));
        }
    }

    private JLabel acceptMessageLabel;
    private JComboBox<AcceptMessageValue> acceptMessageComboBox;
    private JLabel acceptMessageLabel2;
    private JLabel breakEarlyLabel;
    private JRadioButton breakEarlyYesRadio;
    private JRadioButton breakEarlyNoRadio;
}