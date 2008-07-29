package com.webreach.mirth.plugins.pdfviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.util.List;

import com.adobe.acrobat.Viewer;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.plugins.AttachmentViewer;

public class PDFViewer extends AttachmentViewer
{
    public PDFViewer(String name)
    {
        super(name);
    }

    public String getViewerType()
    {
        return "PDF";
    }

    public boolean handleMultiple()
    {
        return false;
    }

    public void viewAttachments(List attachmentIds)
    {
        // do viewing code

    	Frame frame = new Frame("PDF Viewer");
		
		frame.setLayout(new BorderLayout());
		
		try {
			
			final Viewer viewer = new Viewer();
	
			frame.add(viewer, BorderLayout.CENTER);
	
			Attachment attachment = parent.mirthClient.getAttachment((String) attachmentIds.get(0));
			byte[] rawData = attachment.getData();
			ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
	
			viewer.setDocumentInputStream(bis);
	
			viewer.setProperty("Default_Page_Layout", "SinglePage");
			viewer.setProperty("Default_Zoom_Type", "FitPage");
//			viewer.setProperty("Default_Magnification", "100");
			
			
			viewer.activate();
			
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) { 
				    
				    if (viewer != null) {
					
					// The deactivate method will ensure that the
					// acrobat.properties file is saved
					// upon exit.
					
					viewer.deactivate();
				    }
				    
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
