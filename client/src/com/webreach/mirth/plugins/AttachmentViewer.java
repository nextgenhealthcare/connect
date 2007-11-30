package com.webreach.mirth.plugins;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Nov 28, 2007
 * Time: 1:46:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AttachmentViewer {
    public Frame parent = PlatformUI.MIRTH_FRAME;

    public abstract String getViewerType();
    public abstract void viewAttachments(List attachmentIds);
    public abstract boolean handleMultiple();

    public AttachmentViewer(){
    }
}
