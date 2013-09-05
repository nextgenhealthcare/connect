/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.pdfviewer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class MirthPDFViewer extends com.sun.pdfview.PDFViewer {

    public final static String TITLE = "Mirth PDF Viewer";
    private File tempFile;
    
    /**
     * Standard constructor with no file open.
     * @param useThumbs
     */
    public MirthPDFViewer(boolean useThumbs) {
        super(useThumbs);
    }
    
    /**
     * Will open the tempFile and delete it on close.
     * @param useThumbs
     * @param tempFile
     * @throws IOException
     */
    public MirthPDFViewer(boolean useThumbs, File tempFile) throws IOException {
        super(useThumbs);
        super.openFile(tempFile);
        this.tempFile = tempFile;
    }
    
    @Override
    public void doQuit() {
        super.doClose();
        super.dispose();
        
        if (tempFile != null) {
            tempFile.delete();
        }
    }
    
    /**
     * utility method to get an icon from the resources of this class
     * @param name the name of the icon
     * @return the icon, or null if the icon wasn't found.
     */
    @Override
    public Icon getIcon(String name) {
        Icon icon = null;
        URL url = null;
        try {
            url = com.sun.pdfview.PDFViewer.class.getResource(name);

            icon = new ImageIcon(url);
            if (icon == null) {
                System.out.println("Couldn't find " + url);
            }
        } catch (Exception e) {
            System.out.println("Couldn't find " + getClass().getName() + "/" + name);
            e.printStackTrace();
        }
        return icon;
    }
    
    /**
     * Set the title to "Mirth PDF Viewer: <name of file>"
     */
    @Override
    public void setTitle(String title) {
        String newTitle = TITLE;
        int index = title.indexOf(":");
        if (index != -1) {
            newTitle += title.substring(index);
        }
        super.setTitle(newTitle);
    }
}
