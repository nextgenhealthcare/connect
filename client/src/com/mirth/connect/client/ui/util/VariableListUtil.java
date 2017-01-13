/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.util.ScriptBuilderException;

public class VariableListUtil {
    final static String COMMENT_SIMPLE_PATTERN = "//.*";
    final static String COMMENT_BLOCK_PATTERN = "/\\*(?:.|[\\n\\r])*?\\*/";

    /*
     * This regular expression uses alternation to capture either the "xxxxxMap.put" syntax, or the
     * "$x('key'," syntax. Kleene closures for whitespace are used in between every method token
     * since it is legal JavaScript. Instead of checking ['"] once at the beginning and end, it
     * checks once and then uses a backreference later on. That way you can capture keys like
     * "Foo's Bar". It also accounts for backslashes before any subsequent backreferences so that
     * "Foo\"s Bar" would still be captured. In the "$x" case, the regular expression also performs
     * a lookahead to ensure that there is a comma after the first argument, indicating that it is
     * the "put" version of the method, not the "get" version.
     */
    final static String GLOBAL_AND_CHANNEL_VARIABLE_PATTERN = "(?<![A-Za-z0-9_$])(?:channel|global|globalChannel|response)Map\\s*\\.\\s*put\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\1).)*)\\1|(?<![A-Za-z0-9_$])\\$(?:g|gc|c|r)\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\4).)*)\\4(?=\\s*,)";
    final static String LOCAL_VARIABLE_PATTERN = "(?<![A-Za-z0-9_$])(?:channel|global|globalChannel|response|connector)Map\\s*\\.\\s*put\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\1).)*)\\1|(?<![A-Za-z0-9_$])\\$(?:g|gc|c|r|co)\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\4).)*)\\4(?=\\s*,)";
    final static int FULL_NAME_MATCHER_INDEX = 2;
    final static int SHORT_NAME_MATCHER_INDEX = 5;

    public static void getStepVariables(Set<String> targetSet, Transformer transformer, boolean includeLocalVars) {
        getStepVariables(targetSet, transformer, includeLocalVars, -1);
    }

    /*
     * Gets all steps that have variables that should show up in the global variable list
     */
    public static void getStepVariables(Set<String> targetSet, Transformer transformer, boolean includeLocalVars, int row) {

        // add only the global variables
        List<Step> connectorSteps = transformer.getElements();
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
            try {
                String script = stepIterator.next().getScript(false);
                if (StringUtils.isNotEmpty(script)) {
                    String scriptWithoutComments = getScriptWithoutComments(script);

                    Matcher matcher = pattern.matcher(scriptWithoutComments);
                    while (matcher.find()) {
                        targetSet.add(getMapKey(matcher));
                    }
                }
            } catch (ScriptBuilderException e) {
                // Just move on to next step
            }
            currentRow++;
        }
    }

    public static void getRuleVariables(Set<String> targetSet, Filter filter, boolean includeLocalVars) {
        getRuleVariables(targetSet, filter, includeLocalVars, -1);
    }

    /*
     * Gets all rules that have variables that should show up in the global variable list
     */
    public static void getRuleVariables(Set<String> targetSet, Filter filter, boolean includeLocalVars, int row) {

        // add only the global variables
        List<Rule> connectorRules = filter.getElements();
        Iterator<Rule> ruleIterator = connectorRules.iterator();
        String varPattern = GLOBAL_AND_CHANNEL_VARIABLE_PATTERN;
        if (includeLocalVars) {
            varPattern = LOCAL_VARIABLE_PATTERN;
        }
        int currentRow = 0;
        while (ruleIterator.hasNext()) {
            if (row > -1 && row <= currentRow) {
                break;
            }
            Pattern pattern = Pattern.compile(varPattern);
            try {
                String script = ruleIterator.next().getScript(false);
                if (StringUtils.isNotEmpty(script)) {
                    String scriptWithoutComments = getScriptWithoutComments(script);

                    Matcher matcher = pattern.matcher(scriptWithoutComments);
                    while (matcher.find()) {
                        targetSet.add(getMapKey(matcher));
                    }
                }
            } catch (ScriptBuilderException e) {
                // Just move on to next rule
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

    private static String getMapKey(Matcher matcher) {
        /*
         * Since multiple capturing groups are used and the final key could reside on either side of
         * the alternation, we use two specific group indices (2 and 5), one for the full "xxxxxMap"
         * case and one for the short "$x" case. We also replace JavaScript-specific escape
         * sequences like \', \", etc.
         */
        String key = matcher.group(FULL_NAME_MATCHER_INDEX);
        if (key == null) {
            key = matcher.group(SHORT_NAME_MATCHER_INDEX);
        }
        return StringEscapeUtils.unescapeEcmaScript(key);
    }
}
