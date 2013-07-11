/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.JSpinner;

public class MirthBlankableSpinner extends JSpinner {
    public MirthBlankableSpinner() {
        this(null, null);
    }

    public MirthBlankableSpinner(Integer minimum, Integer maximum) {
        setModel(new MirthBlankableSpinnerModel(minimum, maximum));
        setEditor(new MirthBlankableSpinnerEditor(this));
    }

    public Integer getIntegerValue() {
        Object value = getValue();
        return (value == null || value.equals("")) ? null : new Integer(value.toString());
    }
}
