/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.RTextArea;

import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;

public class RSTAPreferences implements Serializable {

    private Map<String, KeyStroke> keyStrokeMap;
    private FindReplaceProperties findReplaceProperties;
    private Map<String, Boolean> toggleOptions;

    public RSTAPreferences() {
        this(null);
    }

    public RSTAPreferences(RSTAPreferences preferences) {
        setDefaultKeyStrokeMap();
        setDefaultToggleOptions();
        setDefaultFindReplaceProperties();

        if (preferences != null) {
            keyStrokeMap.putAll(preferences.getKeyStrokeMap());
            findReplaceProperties = preferences.getFindReplaceProperties();
            toggleOptions.putAll(preferences.getToggleOptions());
        }
    }

    public Map<String, KeyStroke> getKeyStrokeMap() {
        check();
        return keyStrokeMap;
    }

    public void setKeyStrokeMap(Map<String, KeyStroke> keyStrokeMap) {
        this.keyStrokeMap = keyStrokeMap;
    }

    public FindReplaceProperties getFindReplaceProperties() {
        check();
        return findReplaceProperties;
    }

    public void setFindReplaceProperties(FindReplaceProperties findReplaceProperties) {
        this.findReplaceProperties = findReplaceProperties;
    }

    public Map<String, Boolean> getToggleOptions() {
        check();
        return toggleOptions;
    }

    public void setToggleOptions(Map<String, Boolean> toggleOptions) {
        this.toggleOptions = toggleOptions;
    }

    private void check() {
        if (keyStrokeMap == null) {
            setDefaultKeyStrokeMap();
        }

        if (toggleOptions == null) {
            setDefaultToggleOptions();
        }

        if (findReplaceProperties == null) {
            setDefaultFindReplaceProperties();
        }
    }

    private void setDefaultKeyStrokeMap() {
        keyStrokeMap = new HashMap<String, KeyStroke>();

        int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int ctrl = InputEvent.CTRL_MASK;
        int shift = InputEvent.SHIFT_MASK;
        int defaultShift = defaultModifier | shift;
        boolean isOSX = RTextArea.isOSX();

        putKeyStroke(ActionInfo.UNDO, KeyEvent.VK_Z, defaultModifier);
        if (isOSX) {
            putKeyStroke(ActionInfo.REDO, KeyEvent.VK_Z, defaultShift);
        } else {
            putKeyStroke(ActionInfo.REDO, KeyEvent.VK_Y, defaultModifier);
        }
        putKeyStroke(ActionInfo.CUT, KeyEvent.VK_X, defaultModifier);
        putKeyStroke(ActionInfo.COPY, KeyEvent.VK_C, defaultModifier);
        putKeyStroke(ActionInfo.PASTE, KeyEvent.VK_V, defaultModifier);
        putKeyStroke(ActionInfo.DELETE, KeyEvent.VK_DELETE, 0);
        putKeyStroke(ActionInfo.SELECT_ALL, KeyEvent.VK_A, defaultModifier);
        putKeyStroke(ActionInfo.FIND_REPLACE, KeyEvent.VK_F, defaultModifier);
        putKeyStroke(ActionInfo.FIND_NEXT, KeyEvent.VK_G, defaultModifier);
        putKeyStroke(ActionInfo.CLEAR_MARKED_OCCURRENCES, KeyEvent.VK_ESCAPE, 0);
        putKeyStroke(ActionInfo.FOLD_COLLAPSE, KeyEvent.VK_SUBTRACT, defaultModifier);
        putKeyStroke(ActionInfo.FOLD_EXPAND, KeyEvent.VK_ADD, defaultModifier);
        putKeyStroke(ActionInfo.FOLD_COLLAPSE_ALL, KeyEvent.VK_DIVIDE, defaultModifier);
        putKeyStroke(ActionInfo.FOLD_COLLAPSE_ALL_COMMENTS, KeyEvent.VK_DIVIDE, defaultShift);
        putKeyStroke(ActionInfo.FOLD_EXPAND_ALL, KeyEvent.VK_MULTIPLY, defaultModifier);
        putKeyStroke(ActionInfo.AUTO_COMPLETE, KeyEvent.VK_SPACE, ctrl);
        putKeyStroke(ActionInfo.LINE_START, KeyEvent.VK_HOME, 0);
        putKeyStroke(ActionInfo.LINE_END, KeyEvent.VK_END, 0);
        putKeyStroke(ActionInfo.INSERT_LF_BREAK, KeyEvent.VK_ENTER, 0);
        putKeyStroke(ActionInfo.INSERT_CR_BREAK, KeyEvent.VK_ENTER, shift);
    }

    private void putKeyStroke(ActionInfo actionInfo, int keyCode, int modifiers) {
        keyStrokeMap.put(actionInfo.getActionMapKey(), KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    private void setDefaultToggleOptions() {
        toggleOptions = new HashMap<String, Boolean>();
        toggleOptions.put(ActionInfo.DISPLAY_SHOW_TAB_LINES.getActionMapKey(), false);
        toggleOptions.put(ActionInfo.DISPLAY_SHOW_WHITESPACE.getActionMapKey(), false);
        toggleOptions.put(ActionInfo.DISPLAY_SHOW_LINE_ENDINGS.getActionMapKey(), false);
        toggleOptions.put(ActionInfo.DISPLAY_WRAP_LINES.getActionMapKey(), false);
    }

    private void setDefaultFindReplaceProperties() {
        findReplaceProperties = new FindReplaceProperties();
    }
}