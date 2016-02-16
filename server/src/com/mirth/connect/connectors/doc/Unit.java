/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

public enum Unit {
    INCHES("in"), MM("mm"), TWIPS("twips");

    private String value;

    private Unit(String value) {
        this.value = value;
    }

    public double convertTo(double value, Unit unit) {
        return getConversionRate(unit) * value;
    }

    private double getConversionRate(Unit unit) {
        // Switch is not used here to prevent inner class creation
        if (this == INCHES) {
            if (unit == MM) {
                return 25.4;
            } else if (unit == TWIPS) {
                return 1440.0;
            }
        } else if (this == MM) {
            if (unit == INCHES) {
                return 1.0 / 25.4;
            } else if (unit == TWIPS) {
                return 1440.0 / 25.4;
            }
        } else {
            if (unit == INCHES) {
                return 1.0 / 1440.0;
            } else if (unit == MM) {
                return 25.4 / 1440.0;
            }
        }
        return 1;
    }

    @Override
    public String toString() {
        return value;
    }
}
