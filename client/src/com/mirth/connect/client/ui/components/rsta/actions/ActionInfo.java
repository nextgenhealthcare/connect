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
    DELETE_REST_OF_LINE         (RTextAreaEditorKit.rtaDeleteRestOfLineAction),
    DELETE_LINE                 (RTextAreaEditorKit.rtaDeleteLineAction),
    JOIN_LINE                   (RTextAreaEditorKit.rtaJoinLinesAction),
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
    GO_TO_MATCHING_BRACKET      (RSyntaxTextAreaEditorKit.rstaGoToMatchingBracketAction),
    TOGGLE_COMMENT              (RSyntaxTextAreaEditorKit.rstaToggleCommentAction),
    VIEW_USER_API               ("mirth-view-user-api"),
    AUTO_COMPLETE               ("AutoComplete"),
    DOCUMENT_START              (DefaultEditorKit.beginAction),
    DOCUMENT_END                (DefaultEditorKit.endAction),
    DOCUMENT_SELECT_START       (DefaultEditorKit.selectionBeginAction),
    DOCUMENT_SELECT_END         (DefaultEditorKit.selectionEndAction),
    LINE_START                  (DefaultEditorKit.beginLineAction),
    LINE_END                    (DefaultEditorKit.endLineAction),
    LINE_SELECT_START           (DefaultEditorKit.selectionBeginLineAction),
    LINE_SELECT_END             (DefaultEditorKit.selectionEndLineAction),
    MOVE_LEFT                   (DefaultEditorKit.backwardAction),
    MOVE_LEFT_SELECT            (DefaultEditorKit.selectionBackwardAction),
    MOVE_LEFT_WORD              (DefaultEditorKit.previousWordAction),
    MOVE_LEFT_WORD_SELECT       (DefaultEditorKit.selectionPreviousWordAction),
    MOVE_RIGHT                  (DefaultEditorKit.forwardAction),
    MOVE_RIGHT_SELECT           (DefaultEditorKit.selectionForwardAction),
    MOVE_RIGHT_WORD             (DefaultEditorKit.nextWordAction),
    MOVE_RIGHT_WORD_SELECT      (DefaultEditorKit.selectionNextWordAction),
    MOVE_UP                     (DefaultEditorKit.upAction),
    MOVE_UP_SELECT              (DefaultEditorKit.selectionUpAction),
    MOVE_UP_SCROLL              (RTextAreaEditorKit.rtaScrollUpAction),
    MOVE_UP_LINE                (RTextAreaEditorKit.rtaLineUpAction),
    MOVE_DOWN                   (DefaultEditorKit.downAction),
    MOVE_DOWN_SELECT            (DefaultEditorKit.selectionDownAction),
    MOVE_DOWN_SCROLL            (RTextAreaEditorKit.rtaScrollDownAction),
    MOVE_DOWN_LINE              (RTextAreaEditorKit.rtaLineDownAction),
    PAGE_UP                     (DefaultEditorKit.pageUpAction),
    PAGE_UP_SELECT              (RTextAreaEditorKit.rtaSelectionPageUpAction),
    PAGE_LEFT_SELECT            (RTextAreaEditorKit.rtaSelectionPageLeftAction),
    PAGE_DOWN                   (DefaultEditorKit.pageDownAction),
    PAGE_DOWN_SELECT            (RTextAreaEditorKit.rtaSelectionPageDownAction),
    PAGE_RIGHT_SELECT           (RTextAreaEditorKit.rtaSelectionPageRightAction),
    INSERT_LF_BREAK             ("mirth-insert-lf-break"),
    INSERT_CR_BREAK             ("mirth-insert-cr-break"),
    MACRO_BEGIN                 (RTextAreaEditorKit.rtaBeginRecordingMacroAction),
    MACRO_END                   (RTextAreaEditorKit.rtaEndRecordingMacroAction),
    MACRO_PLAYBACK              (RTextAreaEditorKit.rtaPlaybackLastMacroAction);
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