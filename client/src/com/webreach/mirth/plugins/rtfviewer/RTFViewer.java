package com.webreach.mirth.plugins.rtfviewer;

import com.adobe.acrobat.Viewer;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.plugins.AttachmentViewer;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * Date: Jan 22, 2008
 * Time: 10:36:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTFViewer extends AttachmentViewer {
    
    public RTFViewer(String name)
    {
        super(name);
    }

    @Override
    public String getViewerType(){
        return "RTF";
    }
    @Override
    public boolean handleMultiple(){
        return false;
    }
    @Override
    public void viewAttachments(List attachmentIds){
    // do viewing code

    	Frame frame = new Frame("RTF Viewer");
		
		frame.setLayout(new BorderLayout());
		
		try {
	
			Attachment attachment = parent.mirthClient.getAttachment((String) attachmentIds.get(0));
            JEditorPane jEditorPane = new JEditorPane("text/rtf",new String(attachment.getData()));
            jEditorPane.setEditable(false);
            JScrollPane scrollPane = new javax.swing.JScrollPane();
            scrollPane.setViewportView(jEditorPane);            
            frame.add(scrollPane);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) { 
				    e.getWindow().dispose();
				}
			    });
			
			frame.setSize(600, 800);
			
			Dimension dlgSize = frame.getSize();
	        Dimension frmSize = parent.getSize();
	        
	        if (frmSize.width == 0 && frmSize.height == 0) {
	        	frame.setLocationRelativeTo(null);
	        } else {
		        Point loc = parent.getLocation();
		        frame.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
	        }
	        
			frame.setVisible(true);
		} catch (Exception e) {
			parent.alertException(parent, e.getStackTrace(), e.getMessage());
		}
    }
}
