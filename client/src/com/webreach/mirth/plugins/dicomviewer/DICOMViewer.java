package com.webreach.mirth.plugins.dicomviewer;

import com.webreach.mirth.plugins.AttachmentViewer;
import com.webreach.mirth.model.MessageObject;
import sun.misc.BASE64Decoder;

import java.awt.Dimension;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.util.List;

import ij.plugin.DICOM;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Nov 28, 2007
 * Time: 1:49:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMViewer extends AttachmentViewer {

    public DICOMViewer (String name)
    {
        super(name);
    }
    
    public String getViewerType(){
        return "DICOM";
    }
    public boolean handleMultiple(){
        return true;
    }
    public void viewAttachments(List attachmentIds){
    // do viewing code
        try {
            String messageId = parent.mirthClient.getAttachment((String) attachmentIds.get(0)).getMessageId();
            BASE64Decoder decoder = new BASE64Decoder();
            MessageObject message = parent.messageBrowser.getMessageObjectById(messageId);
            byte[] rawImage = decoder.decodeBuffer(parent.mirthClient.getDICOMMessage(message));
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());
            dcm.show();
			Dimension dlgSize = dcm.getWindow().getSize();
	        Dimension frmSize = parent.getSize();
	        Point loc = parent.getLocation();
	        dcm.getWindow().setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        catch(Exception e ){
        	parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }

    }
}
