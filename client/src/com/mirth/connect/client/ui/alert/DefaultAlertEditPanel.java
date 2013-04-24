package com.mirth.connect.client.ui.alert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertModel;
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
        
        enabledCheckBox = new MirthCheckBox("Enabled");
        enabledCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        alertTriggerPane = new DefaultAlertTriggerPane();

        alertActionPane = new AlertActionPane();

        add(nameLabel, "split");
        add(nameTextField, "width 500:500");
        add(enabledCheckBox, "gapbottom 12, wrap");
        add(alertTriggerPane, "grow, height 200:200:, wrap");
        add(alertActionPane, "grow, height 200:200:");
    }
    
    private void updateTasks() {
        parent.setVisibleTasks(parent.alertEditTasks, parent.alertEditPopupMenu, 1, 1, true);
    }
    
    @Override
    public void updateVariableList() {
        List<String> variables = new ArrayList<String>();
        
        variables.add("alertId");
        variables.add("alertName");
        variables.add("systemTime");
        variables.add("serverId");
        variables.add("globalMapVariable");
        
        variables.addAll(alertTriggerPane.getVariables());
        
        alertActionPane.setVariableList(variables);
    }

    @Override
    public void addAlert() {
        AlertModel alertModel = new AlertModel();
        try {
            alertModel.setId(parent.mirthClient.getGuid());
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
        alertModel.setEnabled(true);
        alertModel.setTrigger(new DefaultTrigger(new HashSet<ErrorEventType>(), ""));
        alertModel.getActionGroups().add(new AlertActionGroup());
        
        editAlert(alertModel);
        
        parent.setSaveEnabled(true);
    }

    @Override
    public void editAlert(AlertModel alertModel) {
        nameTextField.setText(alertModel.getName());
        
        enabledCheckBox.setSelected(alertModel.isEnabled());
        
        alertActionPane.setActionGroup(alertModel.getActionGroups().get(0));
        
        alertTriggerPane.setTrigger(alertModel.getTrigger());
        
        updateVariableList();

        this.alertModel = alertModel;
        updateTasks();
    }

    @Override
    public boolean saveAlert() {
        boolean updated = false;
        
        alertModel.setName(nameTextField.getText());
        
        alertModel.setEnabled(enabledCheckBox.isSelected());
        
        alertModel.setTrigger(alertTriggerPane.getTrigger());
        
        // ActionGroups are modified directly so they do not need to be set back to the alert model.
        
        if (StringUtils.isBlank(nameTextField.getText())) {
            parent.alertWarning(parent, "Alert name cannot be empty");
            return false;
        }
        
        if (alertModel.getActionGroups().get(0).getActions().isEmpty()) {
            parent.alertWarning(parent, "Alert requires at least one action.");
            return false;
        }
        
        try {
            parent.mirthClient.updateAlert(alertModel);
            updated = true;
        } catch (ClientException e) {
            parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
        }
        
        return updated;
    }

    private JLabel nameLabel;
    private MirthTextField nameTextField;
    private MirthCheckBox enabledCheckBox;
    private AlertTriggerPane alertTriggerPane;
    private AlertActionPane alertActionPane;
}
