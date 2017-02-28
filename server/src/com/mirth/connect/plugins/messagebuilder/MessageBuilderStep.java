/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagebuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.model.FilterTransformerIterable;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.model.Step;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.JavaScriptSharedUtil.ExprPart;
import com.mirth.connect.util.ScriptBuilderException;

public class MessageBuilderStep extends Step implements FilterTransformerIterable<Step> {

    public static final String PLUGIN_POINT = "Message Builder";

    private String messageSegment;
    private String mapping;
    private String defaultValue;
    private List<Pair<String, String>> replacements;

    public MessageBuilderStep() {
        messageSegment = "";
        mapping = "";
        defaultValue = "";
        replacements = new ArrayList<Pair<String, String>>();
    }

    public MessageBuilderStep(MessageBuilderStep props) {
        super(props);
        messageSegment = props.getMessageSegment();
        mapping = props.getMapping();
        defaultValue = props.getDefaultValue();

        if (props.getReplacements() != null) {
            List<Pair<String, String>> replacements = new ArrayList<Pair<String, String>>();
            for (Pair<String, String> pair : props.getReplacements()) {
                replacements.add(new ImmutablePair<String, String>(pair.getLeft(), pair.getRight()));
            }
            this.replacements = replacements;
        }
    }

    @Override
    public String getScript(boolean loadFiles) {
        String regexArray = buildRegexArray();
        StringBuilder script = new StringBuilder();
        String tempDefaultValue = defaultValue;
        if (StringUtils.isEmpty(tempDefaultValue)) {
            tempDefaultValue = "''";
        }
        String tempMapping = mapping;
        if (StringUtils.isEmpty(tempMapping)) {
            tempMapping = "''";
        }
        script.append(messageSegment);
        script.append(" = ");
        script.append("validate(" + tempMapping + ", " + tempDefaultValue + ", " + regexArray + ");");
        return script.toString();
    }

    @Override
    public String getPreScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        return null;
    }

    @Override
    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        List<ExprPart> exprParts = JavaScriptSharedUtil.getExpressionParts(messageSegment);

        Set<String> indexVariables = new HashSet<String>();
        for (Iterator<IteratorProperties<Step>> it = ancestors.descendingIterator(); it.hasNext();) {
            indexVariables.add(it.next().getIndexVariable());
        }

        // Don't do anything if there aren't at least two parts to the expression
        if (exprParts.size() > 1) {
            // The segment creation logic will be different for E4X XML and regular objects
            script.append("if (typeof(").append(exprParts.get(0)).append(") == 'xml') {\n");

            // Add creation steps for each iterator
            for (Iterator<IteratorProperties<Step>> it = ancestors.descendingIterator(); it.hasNext();) {
                IteratorProperties<Step> ancestor = it.next();
                String indexVar = ancestor.getIndexVariable();
                int currentIndex = getExprIndex(exprParts, indexVar);

                /*
                 * Only add the E4X createSegment calls if the index variable is at least in the
                 * third position (e.g. tmp['OBR'][i]), implying that there exists a base target
                 * object, a segment name, and a position.
                 */
                if (currentIndex > 1) {
                    ExprPart segmentPart = exprParts.get(currentIndex - 1);
                    // Name of the referenced property name before the index variable, e.g. 'OBR'
                    String segmentName = segmentPart.getPropertyName();

                    if (!segmentPart.isNumberLiteral() && !indexVariables.contains(segmentName)) {
                        // Segment including the index variable, e.g. tmp['OBR'][i]
                        String wholeSegment = StringUtils.join(exprParts.subList(0, currentIndex + 1).toArray());
                        script.append("if (typeof(").append(wholeSegment).append(") == 'undefined') {\n");

                        // Segment excluding the index variable and the property before it, e.g. tmp
                        String targetSegment = StringUtils.join(exprParts.subList(0, currentIndex - 1).toArray());
                        // Convert segment name to a string literal if needed
                        if (!StringUtils.startsWithAny(segmentName, "\"", "'")) {
                            segmentName = "'" + StringEscapeUtils.escapeEcmaScript(segmentName) + "'";
                        }
                        script.append("createSegment(").append(segmentName).append(", ").append(targetSegment).append(", ").append(indexVar).append(");\n}\n");
                    }
                }
            }

            script.append("} else {\n");

            /*
             * For regular objects we check every segment up until the second-to-last, because the
             * last one will be set at the end. For each of these creation statements we set the LHS
             * to an empty object, except for segments that occur before index variables which we
             * set to an empty array.
             */
            int lastIndexChecked = -1;

            for (Iterator<IteratorProperties<Step>> it = ancestors.descendingIterator(); it.hasNext();) {
                IteratorProperties<Step> ancestor = it.next();
                String indexVar = ancestor.getIndexVariable();
                int currentIndex = getExprIndex(exprParts, indexVar);

                // Make sure the index variable occurs in at least the second position
                if (currentIndex > 0) {
                    /*
                     * Iterate from the very first segment all the way to the segment associated
                     * with the current index variable. Except that for subsequent iterators, don't
                     * re-do the creation statements for segments already visited.
                     */
                    for (int i = lastIndexChecked + 1; i <= currentIndex; i++) {
                        String targetSegment = StringUtils.join(exprParts.subList(0, i + 1).toArray());
                        script.append("if (typeof(").append(targetSegment).append(") == 'undefined') {\n");

                        // If the segment is right before the index variable or a number literal, create an empty array rather than object
                        String value = "{}";
                        if (i == currentIndex - 1 || (exprParts.size() > i + 1 && (exprParts.get(i + 1).isNumberLiteral() || indexVariables.contains(exprParts.get(i + 1).getPropertyName())))) {
                            value = "[]";
                        }
                        script.append(targetSegment).append(" = ").append(value).append(";\n");
                        script.append("}\n");
                        lastIndexChecked = i;
                    }
                }
            }

            // Create the rest of the segments up until the second-to-last one
            for (int i = lastIndexChecked + 1; i <= exprParts.size() - 2; i++) {
                String targetSegment = StringUtils.join(exprParts.subList(0, i + 1).toArray());
                script.append("if (typeof(").append(targetSegment).append(") == 'undefined') {\n");

                // If the segment is right before a number literal, create an empty array rather than object
                String value = "{}";
                if (exprParts.size() > i + 1 && (exprParts.get(i + 1).isNumberLiteral() || indexVariables.contains(exprParts.get(i + 1).getPropertyName()))) {
                    value = "[]";
                }
                script.append(targetSegment).append(" = ").append(value).append(";\n");
                script.append("}\n");
            }

            script.append("}\n");
        }

        script.append(getScript(loadFiles));
        return script.toString();
    }

    private int getExprIndex(List<ExprPart> exprParts, String indexVar) {
        for (int i = 0; i < exprParts.size(); i++) {
            if (StringUtils.equals(exprParts.get(i).getPropertyName(), indexVar)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getPostScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        return null;
    }

    private String buildRegexArray() {
        StringBuilder regexArray = new StringBuilder();
        regexArray.append("new Array(");
        if (replacements != null && replacements.size() > 0) {
            for (int i = 0; i < replacements.size(); i++) {
                regexArray.append("new Array(" + replacements.get(i).getKey() + ", " + replacements.get(i).getValue() + ")");
                if (i + 1 == replacements.size()) {
                    regexArray.append(")");
                } else {
                    regexArray.append(",");
                }
            }
        } else {
            regexArray.append(")");
        }
        return regexArray.toString();
    }

    public String getMessageSegment() {
        return messageSegment;
    }

    public void setMessageSegment(String messageSegment) {
        this.messageSegment = messageSegment;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<Pair<String, String>> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<Pair<String, String>> replacements) {
        this.replacements = replacements;
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Step clone() {
        return new MessageBuilderStep(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        if (replacements != null) {
            purgedProperties.put("replacementsCount", replacements.size());
        }
        return purgedProperties;
    }
}