/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.purge.Purgable;

public abstract class ChannelTrigger implements Purgable {

    private AlertChannels alertChannels = new AlertChannels();

    public AlertChannels getAlertChannels() {
        return alertChannels;
    }

    public void setAlertChannels(AlertChannels channels) {
        this.alertChannels = channels;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("alertChannels", alertChannels.getPurgedProperties());
        return purgedProperties;
    }
}
