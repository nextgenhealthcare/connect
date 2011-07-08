package com.mirth.connect.connectors.email.client;

import org.apache.commons.net.pop3.POP3Client;

public class Pop3EmailClient implements EmailClient {
    POP3Client client = new POP3Client();

    @Override
    public void login(String username, String password) throws Exception {
        client.login(username, password);
    }

    @Override
    public void listMessages() throws Exception {

    }

    @Override
    public void listMessages(String mailbox) throws Exception {

    }

    @Override
    public void logout() throws Exception {
        client.logout();
    }
}
