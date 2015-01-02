package com.mirth.connect.server.alert.action;

import java.util.List;

public interface Dispatcher {
    /**
     * Dispatches an alert action's subject and content to the given list of recipients.
     */
    public void dispatch(List<String> recipients, String subject, String content) throws DispatchException;
}
