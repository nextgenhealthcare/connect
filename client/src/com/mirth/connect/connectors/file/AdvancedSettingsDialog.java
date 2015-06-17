package com.mirth.connect.connectors.file;

public interface AdvancedSettingsDialog {
    public void setDialogVisible(boolean visible);

    public void setFileSchemeProperties(SchemeProperties properties);

    public SchemeProperties getFileSchemeProperties();

    public SchemeProperties getDefaultProperties();

    public boolean isDefaultProperties();

    public boolean validateProperties();

    public String getSummaryText();
}