package com.mirth.connect.connectors.file;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;

public abstract class AdvancedSettingsDialog extends MirthDialog {

    public AdvancedSettingsDialog() {
        super(PlatformUI.MIRTH_FRAME, true);
    }

    public abstract boolean wasSaved();

    public abstract SchemeProperties getSchemeProperties();
}