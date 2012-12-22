/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import com.mirth.connect.donkey.model.message.Message;

public interface MessageArchiver {
    public void archiveMessage(Message message);

    public boolean isArchived(long messageId);
}
