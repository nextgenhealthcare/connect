/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import com.mirth.connect.donkey.util.migration.Migratable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertAction")
public class AlertAction implements Migratable {

    private AlertActionProtocol protocol;
    private String recipient;
    
    public AlertAction(AlertActionProtocol protocol, String recipient) {
        this.protocol = protocol;
        this.recipient = recipient;
    }

    public AlertActionProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(AlertActionProtocol protocol) {
        this.protocol = protocol;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
