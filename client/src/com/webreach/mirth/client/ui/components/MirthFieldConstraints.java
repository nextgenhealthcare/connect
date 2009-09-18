/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document that can be set with certain field constraints.
 */
public class MirthFieldConstraints extends PlainDocument
{
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
    public MirthFieldConstraints(int limit)
    {
        super();
        this.limit = limit;
        pattern = Pattern.compile(MATCH_ALL_PATTERN);
    }
    
    public MirthFieldConstraints(String newPattern)
    {
        super();
        this.limit = 0;
        pattern = Pattern.compile(newPattern);
    }
    /**
     * Constructor that sets a character number limit, uppercase conversion, letters only, and
     * numbers only. Set limit to 0 for no limit.
     */
    public MirthFieldConstraints(int limit, boolean toUppercase, boolean lettersOnly, boolean numbersOnly)
    {
        super();
        this.limit = limit;
        this.toUppercase = toUppercase;
        String patternString = MATCH_ALL_PATTERN;
        if(lettersOnly && numbersOnly){
            patternString = ALPHA_NUMERIC_PATTERN;
        }
        else if(lettersOnly){
            patternString = ALPHA_PATTERN;
        }
        else if(numbersOnly){
            patternString = NUMERIC_PATTERN;
        }
        pattern = Pattern.compile(patternString);
    }

    /**
     * Overwritten insertString method to check if the string should actually be
     * inserted based on the constraints.
     */
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
    {
        if (str == null) {
            return;
        }
        
        if ((getLength() + str.length()) <= limit || limit == 0) {
            if (toUppercase) {
                str = str.toUpperCase();
            }
            
            Matcher matcher = pattern.matcher(this.getText(0, getLength())+str);
            
            if (!matcher.find()) {
                return;
            }

            super.insertString(offset, str, attr);
        }
    }
}
