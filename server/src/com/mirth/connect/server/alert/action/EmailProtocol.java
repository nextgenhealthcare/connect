package com.mirth.connect.server.alert.action;

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
    public Dispatcher getDispatcher() {
        return new EmailDispatcher();
    }
}
