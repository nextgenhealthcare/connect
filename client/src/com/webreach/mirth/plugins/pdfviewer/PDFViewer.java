package com.webreach.mirth.plugins.attachmentviewer;

import com.webreach.mirth.plugins.AttachmentViewer;
import com.webreach.mirth.model.MessageObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Nov 28, 2007
 * Time: 1:51:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class PDFViewer extends AttachmentViewer {

    public String getViewerType(){
        return "PDF";
    }
    public boolean handleMultiple(){
        return false;
    }
    public void viewAttachments(List attachmentIds){
    // do viewing code
    }
}
