/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.RecordableTextAction;

import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;
import com.mirth.connect.client.ui.components.rsta.actions.MirthRecordableTextAction;

public class CustomJCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {

    private JComponent parent;
    private ActionInfo actionInfo;

    public CustomJCheckBoxMenuItem(JComponent parent, Action action, ActionInfo actionInfo) {
        super(action);
        this.parent = parent;
        this.actionInfo = actionInfo;
        updateAccelerator();
        parent.getActionMap().put(actionInfo.getActionMapKey(), action);
        addActionListener(this);
    }

    public void updateAccelerator() {
        KeyStroke accelerator = MirthInputMap.getInstance().getKeyStroke(actionInfo);
        setAccelerator(accelerator);
        if (getAction() != null && getAction() instanceof RecordableTextAction) {
            ((RecordableTextAction) getAction()).setAccelerator(accelerator);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        // When a check box item is selected, update the user preferences
        MirthRSyntaxTextArea.getRSTAPreferences().getToggleOptions().put(actionInfo.getActionMapKey(), isSelected());
        MirthRSyntaxTextArea.updateToggleOptionPreferences();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (getAction() != null) {
            // Force the action to perform its toggle on/off behavior
            getAction().actionPerformed(new ActionEvent(parent, ActionEvent.ACTION_PERFORMED, selected ? MirthRecordableTextAction.FORCE_ON_COMMAND : MirthRecordableTextAction.FORCE_OFF_COMMAND));
        }
    }
}