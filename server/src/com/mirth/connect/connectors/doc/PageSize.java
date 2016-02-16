/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import static com.mirth.connect.connectors.doc.Unit.INCHES;
import static com.mirth.connect.connectors.doc.Unit.MM;

import org.apache.commons.lang3.text.WordUtils;

public enum PageSize {

    // @formatter:off
    LETTER (8.5f, 11, INCHES),
    LEGAL (8.5f, 14, INCHES),
    LEDGER (11, 17, INCHES),
    TABLOID (17, 11, INCHES),
    EXECUTIVE (7.25f, 10.55f, INCHES),
    ANSI_C (22, 17, INCHES, "ANSI C"),
    ANSI_D (34, 22, INCHES, "ANSI D"),
    ANSI_E (44, 34, INCHES, "ANSI E"),
    A0 (841, 1189, MM),
    A1 (594, 841, MM),
    A2 (420, 594, MM),
    A3 (297, 420, MM),
    A4 (210, 297, MM),
    A5 (148, 210, MM),
    A6 (105, 148, MM),
    A7 (74, 105, MM),
    A8 (52, 74, MM),
    A9 (37, 52, MM),
    A10 (26, 37, MM),
    B0 (1000, 1414, MM),
    B1 (707, 1000, MM),
    B2 (500, 707, MM),
    B3 (353, 500, MM),
    B4 (250, 343, MM),
    B5 (176, 250, MM),
    B6 (125, 176, MM),
    B7 (88, 125, MM),
    B8 (62, 88, MM),
    B9 (44, 62, MM),
    B10 (31, 44, MM),
    CUSTOM (0, 0, MM);
    // @formatter:on

    private float width;
    private float height;
    private Unit unit;
    private String name;

    private PageSize(float width, float height, Unit unit) {
        this(width, height, unit, null);
    }

    private PageSize(float width, float height, Unit unit, String name) {
        this.width = width;
        this.height = height;
        this.unit = unit;
        this.name = name;
    }

    public double getWidth() {
        return width;
    }

    public double getWidth(Unit unit) {
        return this.unit.convertTo(width, unit);
    }

    public double getHeight() {
        return height;
    }

    public double getHeight(Unit unit) {
        return this.unit.convertTo(height, unit);
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return WordUtils.capitalizeFully(super.toString().replace("_", " "));
        }
    }
}