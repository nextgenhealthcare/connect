/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.alert.action;

import java.util.List;
import java.util.Map;

public class EmailProtocol implements Protocol {
    @Override
    public String getName() {
        return "Email";
    }

    @Override
    public Map<String, String> getRecipientOptions() {
        return null;
    }

    @Override
    public List<String> getEmailAddressesForDispatch(List<String> emailAddresses) {
        return emailAddresses;
    }

    @Override
    public void doCustomDispatch(List<String> emailAddresses, String subject, String content) {}
}
