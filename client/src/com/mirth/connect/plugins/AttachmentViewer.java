/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.List;

public abstract class AttachmentViewer extends ClientPlugin {

    public AttachmentViewer(String name) {
        super(name);
    }

    public abstract String getViewerType();

    public abstract void viewAttachments(List<String> attachmentIds, String channelId);

    public abstract boolean handleMultiple();
}
