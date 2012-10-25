/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;

public class VariableListUtil {
    // Finds comments, but also incorrectly finds // or /* inside of block elements like strings.
//	final static String COMMENT_PATTERN = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)";

    final static String COMMENT_SIMPLE_PATTERN = "//.*";
    final static String COMMENT_BLOCK_PATTERN = "/\\*(?:.|[\\n\\r])*?\\*/";
    final static String GLOBAL_AND_CHANNEL_VARIABLE_PATTERN = "(?<![A-Za-z0-9_$])(?:(?:(?:channel|global|globalChannel|response)(?:M|m)ap.put)|\\$(?:g|gc|c|r))\\(\\s*['|\"]([^'|^\"|^\\s]*)[\"|']*";
    final static String LOCAL_VARIABLE_PATTERN = "(?<![A-Za-z0-9_$])(?:(?:(?:channel|global|globalChannel|response|connector)(?:M|m)ap.put)|\\$(?:g|gc|c|r|co))\\(\\s*['|\"]([^'|^\"|^\\s]*)[\"|']*";
    final static int MATCHER_INDEX = 1;

    public static void getStepVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars) {
        getStepVariables(targetSet, connector, includeLocalVars, -1);
    }
    /* 
     * Gets all steps that have variables that should show up in the global variable list
     */

    public static void getStepVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars, int row) {

        // add only the global variables
        List<Step> connectorSteps = connector.getTransformer().getSteps();
        Iterator<Step> stepIterator = connectorSteps.iterator();
        String varPattern = GLOBAL_AND_CHANNEL_VARIABLE_PATTERN;
        if (includeLocalVars) {
            varPattern = LOCAL_VARIABLE_PATTERN;
        }
        int currentRow = 0;
        while (stepIterator.hasNext()) {
            if (row > -1 && row <= currentRow) {
                break;
            }
            Pattern pattern = Pattern.compile(varPattern);
            String scriptWithoutComments = getScriptWithoutComments(stepIterator.next().getScript());

            Matcher matcher = pattern.matcher(scriptWithoutComments);
            while (matcher.find()) {
                targetSet.add(matcher.group(1));
            }
            currentRow++;
        }
    }

    public static void getRuleVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars) {
        getRuleVariables(targetSet, connector, includeLocalVars, -1);
    }
    /* 
     * Gets all rules that have variables that should show up in the global variable list
     */

    public static void getRuleVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars, int row) {

        // add only the global variables
        List<Rule> connectorRules = connector.getFilter().getRules();
        Iterator<Rule> ruleIterator = connectorRules.iterator();
        String varPattern = GLOBAL_AND_CHANNEL_VARIABLE_PATTERN;
        if (includeLocalVars) {
            varPattern = LOCAL_VARIABLE_PATTERN;
        }
        int currentRow = 0;
        while (ruleIterator.hasNext()) {
            if (row > -1 && row >= currentRow) {
                break;
            }
            Pattern pattern = Pattern.compile(varPattern);
            String scriptWithoutComments = getScriptWithoutComments(ruleIterator.next().getScript());

            Matcher matcher = pattern.matcher(scriptWithoutComments);
            while (matcher.find()) {
                targetSet.add(matcher.group(MATCHER_INDEX));
            }
            currentRow++;
        }
    }

    private static String getScriptWithoutComments(String script) {
        String scriptWithoutSimpleComments = null;
        String scriptWithoutCommentBlocks = null;
        String scriptWithoutComments = null;

        try {
            scriptWithoutSimpleComments = script.replaceAll(COMMENT_SIMPLE_PATTERN, "");
            scriptWithoutCommentBlocks = scriptWithoutSimpleComments.replaceAll(COMMENT_BLOCK_PATTERN, "");
        } catch (Throwable e) {
            // Catch stackoverflow bug in java http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
        }

        if (scriptWithoutCommentBlocks != null) {
            scriptWithoutComments = scriptWithoutCommentBlocks;
        } else if (scriptWithoutSimpleComments != null) {
            scriptWithoutComments = scriptWithoutSimpleComments;
        } else {
            scriptWithoutComments = script;
        }

        return scriptWithoutComments;
    }
}
