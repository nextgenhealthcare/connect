package com.webreach.mirth.plugins;

import java.util.List;

public abstract class AttachmentViewer extends ClientPlugin {

    public abstract String getViewerType();

    public abstract void viewAttachments(List attachmentIds);

    public abstract boolean handleMultiple();

    public AttachmentViewer(String name) {
        super(name);
    }
}
