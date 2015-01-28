/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

public class FindReplaceProperties implements Serializable {

    private static final String KEY_FORWARD = "forward";
    private static final String KEY_WRAP_SEARCH = "wrapSearch";
    private static final String KEY_MATCH_CASE = "matchCase";
    private static final String KEY_REGULAR_EXPRESSION = "regularExpression";
    private static final String KEY_WHOLE_WORD = "wholeWord";

    private List<String> findHistory = new ArrayList<String>();
    private List<String> replaceHistory = new ArrayList<String>();
    private Map<String, Boolean> optionsMap = new HashMap<String, Boolean>();

    public FindReplaceProperties() {
        setForward(true);
        setWrapSearch(true);
        setMatchCase(false);
        setRegularExpression(false);
        setWholeWord(false);
    }

    public List<String> getFindHistory() {
        return findHistory;
    }

    public void setFindHistory(List<String> findHistory) {
        this.findHistory = findHistory;
    }

    public List<String> getReplaceHistory() {
        return replaceHistory;
    }

    public void setReplaceHistory(List<String> replaceHistory) {
        this.replaceHistory = replaceHistory;
    }

    public boolean isForward() {
        return getOption(KEY_FORWARD, true);
    }

    public void setForward(boolean forward) {
        putOption(KEY_FORWARD, forward);
    }

    public boolean isWrapSearch() {
        return getOption(KEY_WRAP_SEARCH, true);
    }

    public void setWrapSearch(boolean wrapSearch) {
        putOption(KEY_WRAP_SEARCH, wrapSearch);
    }

    public boolean isMatchCase() {
        return getOption(KEY_MATCH_CASE);
    }

    public void setMatchCase(boolean matchCase) {
        putOption(KEY_MATCH_CASE, matchCase);
    }

    public boolean isRegularExpression() {
        return getOption(KEY_REGULAR_EXPRESSION);
    }

    public void setRegularExpression(boolean regularExpression) {
        putOption(KEY_REGULAR_EXPRESSION, regularExpression);
    }

    public boolean isWholeWord() {
        return getOption(KEY_WHOLE_WORD);
    }

    public void setWholeWord(boolean wholeWord) {
        putOption(KEY_WHOLE_WORD, wholeWord);
    }

    private boolean getOption(String key) {
        return getOption(key, false);
    }

    private boolean getOption(String key, boolean def) {
        return MapUtils.getBooleanValue(optionsMap, key, def);
    }

    private void putOption(String key, boolean value) {
        optionsMap.put(key, value);
    }
}
