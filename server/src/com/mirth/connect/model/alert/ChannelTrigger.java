/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

public abstract class ChannelTrigger {

    private AlertChannels alertChannels = new AlertChannels();

    public AlertChannels getAlertChannels() {
        return alertChannels;
    }

    public void setAlertChannels(AlertChannels channels) {
        this.alertChannels = channels;
    }

}
