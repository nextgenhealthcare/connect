package com.mirth.connect.plugins.messagepruner;

import com.mirth.connect.donkey.model.message.Message;

public interface MessageArchiver {
    public void archiveMessage(Message message);

    public boolean isArchived(long messageId);
}
