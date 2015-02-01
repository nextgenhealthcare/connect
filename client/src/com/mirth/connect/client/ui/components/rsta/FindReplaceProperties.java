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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FindReplaceProperties {

    private static final String KEY_FORWARD = "forward";
    private static final String KEY_WRAP_SEARCH = "wrapSearch";
    private static final String KEY_MATCH_CASE = "matchCase";
    private static final String KEY_REGULAR_EXPRESSION = "regularExpression";
    private static final String KEY_WHOLE_WORD = "wholeWord";

    private List<String> findHistory = new ArrayList<String>();
    private List<String> replaceHistory = new ArrayList<String>();
    private Map<String, Boolean> optionsMap = new HashMap<String, Boolean>();

    public FindReplaceProperties() {
        this(null);
    }

    public FindReplaceProperties(FindReplaceProperties properties) {
        if (properties != null) {
            init(properties.getFindHistory(), properties.getReplaceHistory(), properties.getOptionsMap());
        } else {
            init(null, null, null);
        }
    }

    public FindReplaceProperties(List<String> findHistory, List<String> replaceHistory, Map<String, Boolean> optionsMap) {
        init(findHistory, replaceHistory, optionsMap);
    }

    private void init(List<String> findHistory, List<String> replaceHistory, Map<String, Boolean> optionsMap) {
        setForward(true);
        setWrapSearch(true);
        setMatchCase(false);
        setRegularExpression(false);
        setWholeWord(false);

        if (findHistory != null) {
            this.findHistory = findHistory;
        }
        if (replaceHistory != null) {
            this.replaceHistory = replaceHistory;
        }
        if (optionsMap != null) {
            this.optionsMap.putAll(optionsMap);
        }
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

    protected Map<String, Boolean> getOptionsMap() {
        return optionsMap;
    }

    JsonNode toJsonNode() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();

        ArrayNode findHistoryNode = rootNode.putArray("findHistory");
        for (String element : findHistory) {
            findHistoryNode.add(StringUtils.abbreviate(element, 40));
        }

        ArrayNode replaceHistoryNode = rootNode.putArray("replaceHistory");
        for (String element : replaceHistory) {
            replaceHistoryNode.add(StringUtils.abbreviate(element, 40));
        }

        ObjectNode optionsNode = rootNode.putObject("options");
        for (Entry<String, Boolean> entry : optionsMap.entrySet()) {
            optionsNode.put(entry.getKey(), entry.getValue());
        }

        return rootNode;
    }

    static FindReplaceProperties fromJSON(String findReplaceJSON) {
        List<String> findHistory = new ArrayList<String>();
        List<String> replaceHistory = new ArrayList<String>();
        Map<String, Boolean> optionsMap = new HashMap<String, Boolean>();

        if (StringUtils.isNotBlank(findReplaceJSON)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rootNode = (ObjectNode) mapper.readTree(findReplaceJSON);

                ArrayNode findHistoryNode = (ArrayNode) rootNode.get("findHistory");
                for (Iterator<JsonNode> it = findHistoryNode.elements(); it.hasNext();) {
                    findHistory.add(it.next().asText());
                }

                ArrayNode replaceHistoryNode = (ArrayNode) rootNode.get("replaceHistory");
                for (Iterator<JsonNode> it = replaceHistoryNode.elements(); it.hasNext();) {
                    replaceHistory.add(it.next().asText());
                }

                ObjectNode optionsNode = (ObjectNode) rootNode.get("options");
                for (Iterator<Entry<String, JsonNode>> it = optionsNode.fields(); it.hasNext();) {
                    Entry<String, JsonNode> entry = it.next();

                    if (!entry.getValue().isNull()) {
                        optionsMap.put(entry.getKey(), entry.getValue().asBoolean());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new FindReplaceProperties(findHistory, replaceHistory, optionsMap);
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
