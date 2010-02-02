package com.webreach.mirth.client.ui;

import java.io.File;

public class MirthFileFilter extends javax.swing.filechooser.FileFilter {
    // abstract method

    String type;

    public MirthFileFilter(String type) {
        super();
        this.type = type;
    }

    public boolean accept(File f) {
        // if it is a directory -- we want to show it so return true.
        if (f.isDirectory()) {
            return true;
        }

        // get the extension of the file
        String extension = getExtension(f);

        // check to see if the extension is equal to "xml"
        if (extension.equalsIgnoreCase(type)) {
            return true;
        }

        // default -- fall through. False is return on all
        // occasions except:
        // a) the file is a directory
        // b) the file's extension is what we are looking for.
        return false;
    }

    // abstract method
    public String getDescription() {
        if (type.equalsIgnoreCase("xml")) {
            return "XML files";
        } else if (type.equalsIgnoreCase("html")) {
            return "HTML files";
        } else if (type.equalsIgnoreCase("txt")) {
            return "Text files";
        } else if (type.equalsIgnoreCase("zip")) {
            return "ZIP files";
        } else if (type.equalsIgnoreCase("wsdl")) {
            return "WSDL files";
        } else {
            return "ERROR";
        }
    }

    // Method to get the extension of the file, in lowercase
    private String getExtension(File f) {
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            return s.substring(i + 1).toLowerCase();
        }
        return "";
    }
}
