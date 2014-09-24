/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.alert;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertTrigger;
import com.mirth.connect.model.alert.ChannelTrigger;
import com.mirth.connect.model.alert.DefaultTrigger;

public class DefaultAlertEditPanel extends AlertEditPanel {

    private Frame parent;
    private AlertModel alertModel;

    public DefaultAlertEditPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new MigLayout("insets 10", "grow", "[][grow][grow]"));

        nameLabel = new JLabel("Alert Name: ");
        nameTextField = new MirthTextField();

        nameTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                parent.setPanelName("Edit Alert - " + nameTextField.getText());
            }
        });

        enabledCheckBox = new MirthCheckBox("Enabled");
        enabledCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setLayout(new MigLayout("insets 0"));
        headerPanel.add(nameLabel, "");
        headerPanel.add(nameTextField, "width 500");
        headerPanel.add(enabledCheckBox, "wrap");

        alertTriggerPane = new DefaultAlertTriggerPane();

        alertChannelPane = new AlertChannelPane();

        alertActionPane = new AlertActionPane();

        add(headerPanel, "grow, span");
        add(alertTriggerPane, "grow, sg 1, height 100");
        add(alertChannelPane, "grow, sg 1, height 100, wrap");
        add(alertActionPane, "grow, sgy 1, width 100, height 100, span");
    }

    private void updateTasks() {
        parent.setVisibleTasks(parent.alertEditTasks, parent.alertEditPopupMenu, 1, 1, true);
    }

    @Override
    public void addTriggerType(String name, AlertTriggerPane triggerPane) {
        // This feature is not supported in this implementation.
    }

    @Override
    public String getAlertId() {
        if (alertModel != null) {
            return alertModel.getId();
        }

        return null;
    }

    @Override
    public void updateVariableList() {
        List<String> variables = new ArrayList<String>();

        variables.add("alertId");
        variables.add("alertName");
        variables.add("serverId");
        variables.add("globalMapVariable");
        variables.add("date");

        variables.addAll(alertTriggerPane.getVariables());

        alertActionPane.setVariableList(variables);
    }

    @Override
    public void addAlert() {
        editAlert(new AlertModel(new DefaultTrigger(), new AlertActionGroup()));

        parent.setSaveEnabled(true);
    }

    @Override
    public boolean editAlert(AlertModel alertModel) {
        if (alertModel.getTrigger() instanceof DefaultTrigger) {
            parent.setPanelName("Edit Alert - " + (alertModel.getName() == null ? "" : alertModel.getName()));
            nameTextField.setText(alertModel.getName());

            enabledCheckBox.setSelected(alertModel.isEnabled());

            alertActionPane.setActionGroup(alertModel.getActionGroups().get(0));

            alertTriggerPane.setTrigger(alertModel.getTrigger());
            alertChannelPane.setChannels(((ChannelTrigger) alertModel.getTrigger()).getAlertChannels(), true);

            updateVariableList();

            this.alertModel = alertModel;
            updateTasks();

            return true;
        } else {
            parent.alertError(parent, "This alert cannot be edited. Plugin may be missing.");
            return false;
        }
    }

    @Override
    public boolean saveAlert() {
        boolean updated = false;

        if (StringUtils.isBlank(nameTextField.getText())) {
            parent.alertWarning(parent, "Alert name cannot be empty.");
            return false;
        }

        List<String> triggerValidationErrors = alertTriggerPane.doValidate();
        if (CollectionUtils.isNotEmpty(triggerValidationErrors)) {
            parent.alertWarning(parent, StringUtils.join(triggerValidationErrors, "\n"));
            return false;
        }

        alertModel.setName(nameTextField.getText());

        alertModel.setEnabled(enabledCheckBox.isSelected());

        AlertTrigger trigger = alertTriggerPane.getTrigger();
        ((ChannelTrigger) trigger).setAlertChannels(alertChannelPane.getChannels());

        alertModel.setTrigger(trigger);

        // ActionGroups are modified directly so they do not need to be set back to the alert model.

        try {
            parent.mirthClient.updateAlert(alertModel);
            updated = true;
        } catch (ClientException e) {
            if (e.getMessage().contains("An alert with that name already exists.")) {
                parent.alertWarning(parent, "Alert \"" + alertModel.getName() + "\" already exists.");
            } else {
                parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
            }
        }

        return updated;
    }

    private JLabel nameLabel;
    private MirthTextField nameTextField;
    private MirthCheckBox enabledCheckBox;
    private AlertTriggerPane alertTriggerPane;
    private AlertChannelPane alertChannelPane;
    private AlertActionPane alertActionPane;
}
