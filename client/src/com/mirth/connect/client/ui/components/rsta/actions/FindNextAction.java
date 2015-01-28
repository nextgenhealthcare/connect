/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import com.mirth.connect.client.ui.components.rsta.FindReplaceProperties;
import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class FindNextAction extends MirthRecordableTextAction {

    public FindNextAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.FIND_NEXT);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        FindReplaceProperties findReplaceProperties = MirthRSyntaxTextArea.getRSTAPreferences().getFindReplaceProperties();
        List<String> findHistory = findReplaceProperties.getFindHistory();
        if (CollectionUtils.isEmpty(findHistory)) {
            return;
        }

        SearchContext context = new SearchContext();
        context.setSearchFor(findHistory.get(0));
        context.setSearchForward(findReplaceProperties.isForward());
        context.setMatchCase(findReplaceProperties.isMatchCase());
        context.setRegularExpression(findReplaceProperties.isRegularExpression());
        context.setWholeWord(findReplaceProperties.isWholeWord());

        SearchResult result = SearchEngine.find(textArea, context);

        if (result.getCount() == 0) {
            int position = textArea.getCaretPosition();
            textArea.setCaretPosition(findReplaceProperties.isForward() ? 0 : textArea.getDocument().getLength());
            result = SearchEngine.find(textArea, context);
            if (result.getCount() == 0) {
                textArea.setCaretPosition(position);
            }
        }
    }
}