/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mirth.connect.model.transmission.TransmissionModeProperties;

public abstract class TransmissionModeClientProvider implements DocumentListener, ActionListener {

    public static final String CHANGE_SAMPLE_LABEL_COMMAND = "changesamplelabel";
    public static final String CHANGE_SAMPLE_VALUE_COMMAND = "changesamplevalue";

    protected ActionListener actionListener;

    /**
     * Initialize the plugin with respect to a particular connector. The connector passes in an
     * ActionListener, which the plugin can then use to pass change events to.
     * 
     * @param actionListener
     */
    public void initialize(ActionListener actionListener) {
        this.actionListener = actionListener;
        changeSampleLabel();
    }

    /**
     * Returns the current values set in the implementing UI.
     */
    public abstract TransmissionModeProperties getProperties();

    /**
     * Returns the default properties.
     */
    public abstract TransmissionModeProperties getDefaultProperties();

    /**
     * Uses the given properties object to set the values in the implementing UI.
     * 
     * @param properties
     */
    public abstract void setProperties(TransmissionModeProperties properties);

    /**
     * Determines whether the implementing UI components contain valid values, and optionally
     * highlights any invalid components. If the settings component actually contains fields and
     * such that can be set, then this can be used for validation. If instead the settings component
     * is simply a button that launches a dialog which does its own validation, then this method may
     * just always return true.
     * 
     * @param properties
     *            - The properties object to check for validity
     * @param highlight
     *            - Determines whether to highlight invalid components
     * @return True if all components contain valid values; false otherwise
     */
    public abstract boolean checkProperties(TransmissionModeProperties properties, boolean highlight);

    /**
     * If any invalid components were highlighted, this method resets those backgrounds.
     */
    public abstract void resetInvalidProperties();

    /**
     * Returns the settings component that a connector can use to get/set transmission mode
     * properties. This may be an actual panel with several inner components, or it may be a simple
     * button that launches a separate dialog.
     */
    public abstract JComponent getSettingsComponent();

    /**
     * Returns the current value that should be set for the sample label. This should be a short
     * string explaining what the sample value contains.
     */
    public abstract String getSampleLabel();

    /**
     * Returns the current value that should be set for the sample value. This can contain plain
     * text as well as HTML, but generally it should be a single line briefly enumerating the most
     * relevant properties currently set by the plugin UI.
     */
    public abstract String getSampleValue();

    /**
     * Notifies the ActionListener that the sample label has changed.
     */
    protected final void changeSampleLabel() {
        if (actionListener != null) {
            actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CHANGE_SAMPLE_LABEL_COMMAND));
        }
    }

    /**
     * Notifies the ActionListener that the sample value has changed.
     */
    protected final void changeSampleValue() {
        if (actionListener != null) {
            actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CHANGE_SAMPLE_VALUE_COMMAND));
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        changeSampleValue();
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        changeSampleValue();
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        changeSampleValue();
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        changeSampleValue();
    }
}
