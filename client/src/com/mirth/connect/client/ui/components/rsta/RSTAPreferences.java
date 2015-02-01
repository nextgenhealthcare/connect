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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.KeyStroke;

import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rtextarea.RTextArea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;

public class RSTAPreferences {

    private Map<String, KeyStroke> keyStrokeMap;
    private FindReplaceProperties findReplaceProperties;
    private Map<String, Boolean> toggleOptions;
    private AutoCompleteProperties autoCompleteProperties;

    public RSTAPreferences() {
        this(null, null, null, null);
    }

    public RSTAPreferences(Map<String, KeyStroke> keyStrokeMap, FindReplaceProperties findReplaceProperties, Map<String, Boolean> toggleOptions, AutoCompleteProperties autoCompleteProperties) {
        setDefaultKeyStrokeMap();
        setDefaultFindReplaceProperties();
        setDefaultToggleOptions();
        setDefaultAutoCompleteProperties();

        if (keyStrokeMap != null) {
            this.keyStrokeMap.putAll(keyStrokeMap);
        }
        if (findReplaceProperties != null) {
            this.findReplaceProperties = new FindReplaceProperties(findReplaceProperties);
        }
        if (toggleOptions != null) {
            this.toggleOptions.putAll(toggleOptions);
        }
        if (autoCompleteProperties != null) {
            this.autoCompleteProperties = new AutoCompleteProperties(autoCompleteProperties);
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

    public AutoCompleteProperties getAutoCompleteProperties() {
        check();
        return autoCompleteProperties;
    }

    public void setAutoCompleteProperties(AutoCompleteProperties autoCompleteProperties) {
        this.autoCompleteProperties = autoCompleteProperties;
    }

    String getKeyStrokesJSON() {
        ObjectNode keyStrokesNode = JsonNodeFactory.instance.objectNode();

        for (Entry<String, KeyStroke> entry : getKeyStrokeMap().entrySet()) {
            if (entry.getValue() != null) {
                ArrayNode arrayNode = keyStrokesNode.putArray(entry.getKey());
                arrayNode.add(entry.getValue().getKeyCode());
                arrayNode.add(entry.getValue().getModifiers());
            } else {
                keyStrokesNode.putNull(entry.getKey());
            }
        }

        return keyStrokesNode.toString();
    }

    String getFindReplaceJSON() {
        return getFindReplaceProperties().toJsonNode().toString();
    }

    String getToggleOptionsJSON() {
        ObjectNode toggleOptionsNode = JsonNodeFactory.instance.objectNode();
        for (Entry<String, Boolean> entry : getToggleOptions().entrySet()) {
            toggleOptionsNode.put(entry.getKey(), entry.getValue());
        }
        return toggleOptionsNode.toString();
    }

    String getAutoCompleteJSON() {
        return getAutoCompleteProperties().toJsonNode().toString();
    }

    static RSTAPreferences fromJSON(String keyStrokesJSON, String findReplaceJSON, String toggleOptionsJSON, String autoCompleteJSON) {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, KeyStroke> keyStrokeMap = new HashMap<String, KeyStroke>();
        if (StringUtils.isNotBlank(keyStrokesJSON)) {
            try {
                ObjectNode keyStrokesNode = (ObjectNode) mapper.readTree(keyStrokesJSON);

                for (Iterator<Entry<String, JsonNode>> it = keyStrokesNode.fields(); it.hasNext();) {
                    Entry<String, JsonNode> entry = it.next();
                    KeyStroke keyStroke = null;

                    if (!entry.getValue().isNull()) {
                        ArrayNode arrayNode = (ArrayNode) entry.getValue();
                        if (arrayNode.size() > 1) {
                            keyStroke = KeyStroke.getKeyStroke(arrayNode.get(0).asInt(), arrayNode.get(1).asInt());
                        }
                    }

                    keyStrokeMap.put(entry.getKey(), keyStroke);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FindReplaceProperties findReplaceProperties = FindReplaceProperties.fromJSON(findReplaceJSON);

        Map<String, Boolean> toggleOptions = new HashMap<String, Boolean>();
        if (StringUtils.isNotBlank(toggleOptionsJSON)) {
            try {
                ObjectNode toggleOptionsNode = (ObjectNode) mapper.readTree(toggleOptionsJSON);

                for (Iterator<Entry<String, JsonNode>> it = toggleOptionsNode.fields(); it.hasNext();) {
                    Entry<String, JsonNode> entry = it.next();

                    if (!entry.getValue().isNull()) {
                        toggleOptions.put(entry.getKey(), entry.getValue().asBoolean());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AutoCompleteProperties autoCompleteProperties = AutoCompleteProperties.fromJSON(autoCompleteJSON);

        return new RSTAPreferences(keyStrokeMap, findReplaceProperties, toggleOptions, autoCompleteProperties);
    }

    private void check() {
        if (keyStrokeMap == null) {
            setDefaultKeyStrokeMap();
        }

        if (findReplaceProperties == null) {
            setDefaultFindReplaceProperties();
        }

        if (toggleOptions == null) {
            setDefaultToggleOptions();
        }

        if (autoCompleteProperties == null) {
            setDefaultAutoCompleteProperties();
        }
    }

    private void setDefaultKeyStrokeMap() {
        keyStrokeMap = new HashMap<String, KeyStroke>();

        boolean isOSX = RTextArea.isOSX();
        int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int ctrl = InputEvent.CTRL_MASK;
        int alt = InputEvent.ALT_MASK;
        int shift = InputEvent.SHIFT_MASK;
        int defaultShift = defaultModifier | shift;
        int moveByWordMod = isOSX ? alt : defaultModifier;
        int moveByWordModShift = moveByWordMod | shift;

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
        putKeyStroke(ActionInfo.DELETE_REST_OF_LINE, KeyEvent.VK_DELETE, defaultModifier);
        putKeyStroke(ActionInfo.DELETE_LINE, KeyEvent.VK_D, defaultModifier);
        putKeyStroke(ActionInfo.JOIN_LINE, KeyEvent.VK_J, defaultModifier);
        putKeyStroke(ActionInfo.SELECT_ALL, KeyEvent.VK_A, defaultModifier);
        putKeyStroke(ActionInfo.FIND_REPLACE, KeyEvent.VK_F, defaultModifier);
        putKeyStroke(ActionInfo.FIND_NEXT, KeyEvent.VK_G, defaultModifier);
        putKeyStroke(ActionInfo.CLEAR_MARKED_OCCURRENCES, KeyEvent.VK_ESCAPE, 0);
        putKeyStroke(ActionInfo.FOLD_COLLAPSE, KeyEvent.VK_SUBTRACT, defaultModifier);
        putKeyStroke(ActionInfo.FOLD_EXPAND, KeyEvent.VK_ADD, defaultModifier);
        putKeyStroke(ActionInfo.FOLD_COLLAPSE_ALL, KeyEvent.VK_DIVIDE, defaultModifier);
        putKeyStroke(ActionInfo.FOLD_COLLAPSE_ALL_COMMENTS, KeyEvent.VK_DIVIDE, defaultShift);
        putKeyStroke(ActionInfo.FOLD_EXPAND_ALL, KeyEvent.VK_MULTIPLY, defaultModifier);
        putKeyStroke(ActionInfo.GO_TO_MATCHING_BRACKET, KeyEvent.VK_OPEN_BRACKET, defaultModifier);
        putKeyStroke(ActionInfo.TOGGLE_COMMENT, KeyEvent.VK_SLASH, defaultModifier);
        putKeyStroke(ActionInfo.AUTO_COMPLETE, KeyEvent.VK_SPACE, ctrl);

        if (isOSX) {
            putKeyStroke(ActionInfo.DOCUMENT_START, KeyEvent.VK_HOME, 0);
            putKeyStroke(ActionInfo.DOCUMENT_END, KeyEvent.VK_END, 0);
            putKeyStroke(ActionInfo.DOCUMENT_SELECT_START, KeyEvent.VK_HOME, shift);
            putKeyStroke(ActionInfo.DOCUMENT_SELECT_END, KeyEvent.VK_END, shift);
            putKeyStroke(ActionInfo.LINE_START, KeyEvent.VK_LEFT, defaultModifier);
            putKeyStroke(ActionInfo.LINE_END, KeyEvent.VK_RIGHT, defaultModifier);
            putKeyStroke(ActionInfo.LINE_SELECT_START, KeyEvent.VK_LEFT, defaultShift);
            putKeyStroke(ActionInfo.LINE_SELECT_END, KeyEvent.VK_RIGHT, defaultShift);
        } else {
            putKeyStroke(ActionInfo.DOCUMENT_START, KeyEvent.VK_HOME, defaultModifier);
            putKeyStroke(ActionInfo.DOCUMENT_END, KeyEvent.VK_END, defaultModifier);
            putKeyStroke(ActionInfo.DOCUMENT_SELECT_START, KeyEvent.VK_HOME, defaultShift);
            putKeyStroke(ActionInfo.DOCUMENT_SELECT_END, KeyEvent.VK_END, defaultShift);
            putKeyStroke(ActionInfo.LINE_START, KeyEvent.VK_HOME, 0);
            putKeyStroke(ActionInfo.LINE_END, KeyEvent.VK_END, 0);
            putKeyStroke(ActionInfo.LINE_SELECT_START, KeyEvent.VK_HOME, shift);
            putKeyStroke(ActionInfo.LINE_SELECT_END, KeyEvent.VK_END, shift);
        }

        putKeyStroke(ActionInfo.MOVE_LEFT, KeyEvent.VK_LEFT, 0);
        putKeyStroke(ActionInfo.MOVE_LEFT_SELECT, KeyEvent.VK_LEFT, shift);
        putKeyStroke(ActionInfo.MOVE_LEFT_WORD, KeyEvent.VK_LEFT, moveByWordMod);
        putKeyStroke(ActionInfo.MOVE_LEFT_WORD_SELECT, KeyEvent.VK_LEFT, moveByWordModShift);
        putKeyStroke(ActionInfo.MOVE_RIGHT, KeyEvent.VK_RIGHT, 0);
        putKeyStroke(ActionInfo.MOVE_RIGHT_SELECT, KeyEvent.VK_RIGHT, shift);
        putKeyStroke(ActionInfo.MOVE_RIGHT_WORD, KeyEvent.VK_RIGHT, moveByWordMod);
        putKeyStroke(ActionInfo.MOVE_RIGHT_WORD_SELECT, KeyEvent.VK_RIGHT, moveByWordModShift);
        putKeyStroke(ActionInfo.MOVE_UP, KeyEvent.VK_UP, 0);
        putKeyStroke(ActionInfo.MOVE_UP_SELECT, KeyEvent.VK_UP, shift);
        putKeyStroke(ActionInfo.MOVE_UP_SCROLL, KeyEvent.VK_UP, defaultModifier);
        putKeyStroke(ActionInfo.MOVE_UP_LINE, KeyEvent.VK_UP, alt);
        putKeyStroke(ActionInfo.MOVE_DOWN, KeyEvent.VK_DOWN, 0);
        putKeyStroke(ActionInfo.MOVE_DOWN_SELECT, KeyEvent.VK_DOWN, shift);
        putKeyStroke(ActionInfo.MOVE_DOWN_SCROLL, KeyEvent.VK_DOWN, defaultModifier);
        putKeyStroke(ActionInfo.MOVE_DOWN_LINE, KeyEvent.VK_DOWN, alt);
        putKeyStroke(ActionInfo.PAGE_UP, KeyEvent.VK_PAGE_UP, 0);
        putKeyStroke(ActionInfo.PAGE_UP_SELECT, KeyEvent.VK_PAGE_UP, shift);
        putKeyStroke(ActionInfo.PAGE_LEFT_SELECT, KeyEvent.VK_PAGE_UP, defaultShift);
        putKeyStroke(ActionInfo.PAGE_DOWN, KeyEvent.VK_PAGE_DOWN, 0);
        putKeyStroke(ActionInfo.PAGE_DOWN_SELECT, KeyEvent.VK_PAGE_DOWN, shift);
        putKeyStroke(ActionInfo.PAGE_RIGHT_SELECT, KeyEvent.VK_PAGE_DOWN, defaultShift);
        putKeyStroke(ActionInfo.INSERT_LF_BREAK, KeyEvent.VK_ENTER, 0);
        putKeyStroke(ActionInfo.INSERT_CR_BREAK, KeyEvent.VK_ENTER, shift);
        putKeyStroke(ActionInfo.MACRO_BEGIN, KeyEvent.VK_B, defaultShift);
        putKeyStroke(ActionInfo.MACRO_END, KeyEvent.VK_N, defaultShift);
        putKeyStroke(ActionInfo.MACRO_PLAYBACK, KeyEvent.VK_M, defaultShift);
    }

    private void putKeyStroke(ActionInfo actionInfo, int keyCode, int modifiers) {
        keyStrokeMap.put(actionInfo.getActionMapKey(), KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    private void setDefaultFindReplaceProperties() {
        findReplaceProperties = new FindReplaceProperties();
    }

    private void setDefaultToggleOptions() {
        toggleOptions = new HashMap<String, Boolean>();
        toggleOptions.put(ActionInfo.DISPLAY_SHOW_TAB_LINES.getActionMapKey(), false);
        toggleOptions.put(ActionInfo.DISPLAY_SHOW_WHITESPACE.getActionMapKey(), false);
        toggleOptions.put(ActionInfo.DISPLAY_SHOW_LINE_ENDINGS.getActionMapKey(), false);
        toggleOptions.put(ActionInfo.DISPLAY_WRAP_LINES.getActionMapKey(), false);
    }

    private void setDefaultAutoCompleteProperties() {
        autoCompleteProperties = new AutoCompleteProperties();
    }
}