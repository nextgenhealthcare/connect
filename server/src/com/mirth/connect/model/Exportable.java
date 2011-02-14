package com.mirth.connect.model;

public interface Exportable {
    public static final String DATE_TIME_FORMAT = "dd-MM-yy HH-mm-ss.SS";
    
    public String toExportString();
}
