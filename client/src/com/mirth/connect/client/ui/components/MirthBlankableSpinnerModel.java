/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.AbstractSpinnerModel;

public class MirthBlankableSpinnerModel extends AbstractSpinnerModel {
    private Object value = "";
    private Integer minimum;
    private Integer maximum;
    private int startValue;

    public MirthBlankableSpinnerModel() {
        this(null, null);
    }

    public MirthBlankableSpinnerModel(Integer minimum, Integer maximum) {
        this.minimum = minimum;
        this.maximum = maximum;

        if (minimum == null && maximum == null) {
            startValue = 0;
        } else if (minimum != null) {
            startValue = minimum;
        } else {
            startValue = maximum;
        }
    }

    @Override
    public Object getNextValue() {
        return (value.equals("")) ? startValue : min(new Integer(value.toString()) + 1, maximum);
    }

    @Override
    public Object getPreviousValue() {
        return (value.equals("")) ? startValue : max(new Integer(value.toString()) - 1, minimum);
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

                if (minimum != null && (Integer) value < minimum) {
                    value = minimum;
                } else if (maximum != null && (Integer) value > maximum) {
                    value = maximum;
                }
            } catch (NumberFormatException e) {
                value = "";
            }
        }

        fireStateChanged();
    }

    private int min(Integer a, Integer b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            return Math.min(a, b);
        }
    }

    private int max(Integer a, Integer b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            return Math.max(a, b);
        }
    }
}
