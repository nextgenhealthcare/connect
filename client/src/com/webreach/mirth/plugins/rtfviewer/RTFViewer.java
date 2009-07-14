package com.webreach.mirth.plugins.rtfviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.plugins.AttachmentViewer;
import sun.misc.BASE64Decoder;

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
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] rawRTF = decoder.decodeBuffer(new String(attachment.getData()));
            JEditorPane jEditorPane = new JEditorPane("text/rtf", new String(rawRTF));

			if (jEditorPane.getDocument().getLength() == 0) {
				// decoded when it should not have been.  i.e.) the attachment data was not encoded.
				jEditorPane.setText(new String(attachment.getData()));
			}
			
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
	        Point loc = parent.getLocation();
	        
	        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
	        	frame.setLocationRelativeTo(null);
	        } else {
		        frame.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
	        }
	        
			frame.setVisible(true);
		} catch (Exception e) {
			parent.alertException(parent, e.getStackTrace(), e.getMessage());
		}
    }
}
