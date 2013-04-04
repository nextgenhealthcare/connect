/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document that can be set with certain field constraints.
 */
public class MirthFieldConstraints extends PlainDocument {

    private int limit;
    // optional uppercase conversion, letters only, and numbers only.
    private boolean toUppercase = false;
    private Pattern pattern;
    private static final String NUMERIC_PATTERN = "^[0-9]*$";
    private static final String ALPHA_PATTERN = "^[a-zA-Z_\\-\\s]*$";
    private static final String ALPHA_NUMERIC_PATTERN = "^[a-zA-Z_0-9\\-\\s]*$";
    private static final String MATCH_ALL_PATTERN = "^.*$";

    /**
     * Constructor that sets a character number limit. Set limit to 0 for no
     * limit.
     */
    public MirthFieldConstraints(int limit) {
        super();
        this.limit = limit;
        pattern = Pattern.compile(MATCH_ALL_PATTERN);
    }

    public MirthFieldConstraints(String newPattern) {
        super();
        this.limit = 0;
        pattern = Pattern.compile(newPattern);
    }

    /**
     * Constructor that sets a character number limit, uppercase conversion, letters only, and
     * numbers only. Set limit to 0 for no limit.
     */
    public MirthFieldConstraints(int limit, boolean toUppercase, boolean lettersOnly, boolean numbersOnly) {
        super();
        this.limit = limit;
        this.toUppercase = toUppercase;
        String patternString = MATCH_ALL_PATTERN;
        if (lettersOnly && numbersOnly) {
            patternString = ALPHA_NUMERIC_PATTERN;
        } else if (lettersOnly) {
            patternString = ALPHA_PATTERN;
        } else if (numbersOnly) {
            patternString = NUMERIC_PATTERN;
        }
        pattern = Pattern.compile(patternString);
    }

    /**
     * Overwritten insertString method to check if the string should actually be
     * inserted based on the constraints.
     */
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }

        if ((getLength() + str.length()) <= limit || limit == 0) {
            if (toUppercase) {
                str = str.toUpperCase();
            }

            Matcher matcher = pattern.matcher(this.getText(0, getLength()) + str);

            if (!matcher.find()) {
                return;
            }

            super.insertString(offset, str, attr);
        }
    }
}
