/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import javax.swing.text.DefaultEditorKit;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rtextarea.RTextAreaEditorKit;

public enum ActionInfo {
    // @formatter:off
    UNDO                        (RTextAreaEditorKit.rtaUndoAction),
    REDO                        (RTextAreaEditorKit.rtaRedoAction),
    CUT                         (DefaultEditorKit.cutAction),
    COPY                        (DefaultEditorKit.copyAction),
    PASTE                       (DefaultEditorKit.pasteAction),
    DELETE                      (DefaultEditorKit.deleteNextCharAction),
    SELECT_ALL                  (DefaultEditorKit.selectAllAction),
    FIND_REPLACE                ("mirth-find-replace"),
    FIND_NEXT                   ("mirth-find-next"),
    CLEAR_MARKED_OCCURRENCES    ("mirth-clear-marked-occurrences"),
    FOLD_COLLAPSE               (RSyntaxTextAreaEditorKit.rstaCollapseFoldAction),
    FOLD_EXPAND                 (RSyntaxTextAreaEditorKit.rstaExpandFoldAction),
    FOLD_COLLAPSE_ALL           (RSyntaxTextAreaEditorKit.rstaCollapseAllFoldsAction),
    FOLD_COLLAPSE_ALL_COMMENTS  (RSyntaxTextAreaEditorKit.rstaCollapseAllCommentFoldsAction),
    FOLD_EXPAND_ALL             (RSyntaxTextAreaEditorKit.rstaExpandAllFoldsAction),
    DISPLAY_SHOW_TAB_LINES      ("mirth-show-tab-lines"),
    DISPLAY_SHOW_WHITESPACE     ("mirth-show-whitespace"),
    DISPLAY_SHOW_LINE_ENDINGS   ("mirth-show-line-endings"),
    DISPLAY_WRAP_LINES          ("mirth-wrap-lines"),
    VIEW_USER_API               ("mirth-view-user-api"),
    AUTO_COMPLETE               ("AutoComplete"),
    LINE_START                  (DefaultEditorKit.beginLineAction),
    LINE_END                    (DefaultEditorKit.endLineAction),
    INSERT_LF_BREAK             ("mirth-insert-lf-break"),
    INSERT_CR_BREAK             ("mirth-insert-cr-break");
    // @formatter:on

    private String actionMapKey;

    private ActionInfo(String actionMapKey) {
        this.actionMapKey = actionMapKey;
    }

    public String getActionMapKey() {
        return actionMapKey;
    }

    public static ActionInfo fromActionMapKey(String actionMapKey) {
        for (ActionInfo actionInfo : values()) {
            if (actionInfo.getActionMapKey().equals(actionMapKey)) {
                return actionInfo;
            }
        }
        return null;
    }
}