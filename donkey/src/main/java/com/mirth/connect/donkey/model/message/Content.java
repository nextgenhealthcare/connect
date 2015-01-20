package com.mirth.connect.donkey.model.message;

public abstract class Content {
    private boolean encrypted = false;

    public Content() {}

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getContent() {
        return "";
    }

    public static Content getContent(Message message, Integer metaDataId, ContentType contentType) {
        Content content = null;

        if (contentType == ContentType.CHANNEL_MAP) {
            content = message.getMergedConnectorMessage().getChannelMapContent();
        } else if (contentType == ContentType.SOURCE_MAP) {
            content = message.getMergedConnectorMessage().getSourceMapContent();
        } else if (contentType == ContentType.RESPONSE_MAP) {
            content = message.getMergedConnectorMessage().getResponseMapContent();
        } else {
            content = message.getConnectorMessages().get(metaDataId).getContent(contentType);
        }

        return content;
    }
}
