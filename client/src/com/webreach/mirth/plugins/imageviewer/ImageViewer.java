package com.webreach.mirth.plugins.imageviewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.plugins.AttachmentViewer;

public class ImageViewer extends AttachmentViewer {

	private BufferedImage image;
	
     public ImageViewer(String name) {
            super(name);
    }
    
	public String getViewerType() {
		return "Image";
	}

	public boolean handleMultiple() {
		return false;
	}

	public void viewAttachments(List attachmentIds) {

		JFrame frame = new JFrame("Image Viewer");

		try {

			Attachment attachment = parent.mirthClient.getAttachment((String) attachmentIds.get(0));
			byte[] rawData = attachment.getData();
			ByteArrayInputStream bis = new ByteArrayInputStream(rawData);

			image = ImageIO.read(bis);

			JScrollPane pictureScrollPane = new JScrollPane(new JLabel(new ImageIcon(image)));
			pictureScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			pictureScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			frame.add(pictureScrollPane);

			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});

			frame.pack();

			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();

			// Resize the frame so that it fits and scrolls images larger than
			// 800x600 properly.
			if (imageWidth > 800 || imageHeight > 600) {
				int width = imageWidth;
				int height = imageHeight;
				if (imageWidth > 800)
					width = 800;
				if (imageHeight > 600)
					height = 600;

				// Also add the scrollbars to the window width if necessary.
				Integer scrollBarWidth = (Integer) UIManager.get("ScrollBar.width");
				int verticalScrollBar = 0;
				int horizontalScrollBar = 0;

				if (width == 800)
					horizontalScrollBar = scrollBarWidth.intValue();
				if (height == 600)
					verticalScrollBar = scrollBarWidth.intValue();

				// Also add the window borders to the width.
				width = width + frame.getInsets().left + frame.getInsets().right + verticalScrollBar;
				height = height + frame.getInsets().top + frame.getInsets().bottom + horizontalScrollBar;

				frame.setSize(width, height);
			}

			Dimension dlgSize = frame.getSize();
			Dimension frmSize = parent.getSize();
			Point loc = parent.getLocation();
			frame.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
			frame.setVisible(true);
		} catch (Exception e) {
			parent.alertException(e.getStackTrace(), e.getMessage());
		}
	}
}
