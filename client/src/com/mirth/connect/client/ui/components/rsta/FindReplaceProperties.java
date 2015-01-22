/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.util.ArrayList;
import java.util.List;

public class FindReplaceProperties {

    private List<String> findHistory = new ArrayList<String>();
    private List<String> replaceHistory = new ArrayList<String>();
    private boolean forward = true;
    private boolean wrapSearch = true;
    private boolean matchCase;
    private boolean regularExpression;
    private boolean wholeWord;

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
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public boolean isWrapSearch() {
        return wrapSearch;
    }

    public void setWrapSearch(boolean wrapSearch) {
        this.wrapSearch = wrapSearch;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }
}
