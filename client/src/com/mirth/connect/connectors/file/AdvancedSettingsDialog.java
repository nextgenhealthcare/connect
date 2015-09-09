/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

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