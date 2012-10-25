/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.AbstractSpinnerModel;

public class MirthBlankableSpinnerModel extends AbstractSpinnerModel {
    private Object value = "";
    
    @Override
    public Object getNextValue() {
        return (value.equals("")) ? 0 : new Integer(value.toString()) + 1;
    }

    @Override
    public Object getPreviousValue() {
        return (value.equals("")) ? -1 : new Integer(value.toString()) - 1;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object object) {
        if (object == null) {
            value = null;
        } else {
            try {
                value = new Integer(object.toString());
            } catch (NumberFormatException e) {
                value = "";
            }
        }
        
        fireStateChanged();
    }
}
