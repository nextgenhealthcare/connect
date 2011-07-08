package com.mirth.connect.connectors.email.client;

public interface EmailClient {
    public void login(String username, String password) throws Exception;

    public void listMessages() throws Exception;

    public void listMessages(String mailbox) throws Exception;

    public void logout() throws Exception;
}
