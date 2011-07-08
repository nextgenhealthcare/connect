package com.mirth.connect.connectors.email.client;

import org.apache.commons.net.imap.IMAPClient;

public class ImapEmailClient implements EmailClient {

    private IMAPClient client = new IMAPClient();

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
