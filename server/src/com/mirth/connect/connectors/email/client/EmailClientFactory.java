package com.mirth.connect.connectors.email.client;

public class EmailClientFactory {
    public static EmailClient createEmailClient(boolean isImap) {
        if (isImap) {
            return new ImapEmailClient();
        } else {
            return new Pop3EmailClient();
        }
    }
}
