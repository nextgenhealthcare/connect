package com.webreach.mirth.plugins;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Nov 28, 2007
 * Time: 1:46:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AttachmentViewer extends ClientPlugin {

    public abstract String getViewerType();
    public abstract void viewAttachments(List attachmentIds);
    public abstract boolean handleMultiple();
   
    public AttachmentViewer(String name){
        super(name);
    }
}
