package com.mirth.connect.server.alert.action;

import java.util.Map;

public interface Protocol {
    /**
     * Returns the unique name of the alert action protocol.
     */
    public String getName();

    /**
     * Returns a map of id -> name recipients or null if this protocol allows free text input of
     * recipients.
     */
    public Map<String, String> getRecipientOptions();

    /**
     * Returns the dispatcher implementation to use for this protocol.
     */
    public Dispatcher getDispatcher();
}
