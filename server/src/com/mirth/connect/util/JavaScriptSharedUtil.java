/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.mozilla.javascript.Context;

public class JavaScriptSharedUtil {

    private final static String RESULT_PATTERN = "responseMap\\s*\\.\\s*put\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\1).)*)(?<!\\\\)\\1|\\$r\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\4).)*)(?<!\\\\)\\4(?=\\s*,)";
    private final static int FULL_NAME_MATCHER_INDEX = 2;
    private final static int SHORT_NAME_MATCHER_INDEX = 5;

    /*
     * Retrieves the Context for the current Thread. The context must be cleaned up with
     * Context.exit() when it is no longer needed.
     */
    public static Context getGlobalContextForValidation() {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        return context;
    }

    /*
     * This regular expression uses alternation to capture either the "responseMap.put" syntax, or
     * the "$r('key'," syntax. Kleene closures for whitespace are used in between every method token
     * since it is legal JavaScript. Instead of checking ['"] once at the beginning and end, it
     * checks once and then uses a backreference later on. That way you can capture keys like
     * "Foo's Bar". It also accounts for backslashes before any subsequent backreferences so that
     * "Foo\"s Bar" would still be captured. In the "$r" case, the regular expression also performs
     * a lookahead to ensure that there is a comma after the first argument, indicating that it is
     * the "put" version of the method, not the "get" version.
     */
    public static Collection<String> getResponseVariables(String script) {
        Collection<String> variables = new HashSet<String>();

        Pattern pattern = Pattern.compile(RESULT_PATTERN);
        if (script != null && script.length() > 0) {
            Matcher matcher = pattern.matcher(script);
            while (matcher.find()) {
                variables.add(getMapKey(matcher));
            }
        }
        return variables;
    }

    private static String getMapKey(Matcher matcher) {
        /*
         * Since multiple capturing groups are used and the final key could reside on either side of
         * the alternation, we use two specific group indices (2 and 5), one for the full
         * "responseMap" case and one for the short "$r" case. We also replace JavaScript-specific
         * escape sequences like \', \", etc.
         */
        String key = matcher.group(FULL_NAME_MATCHER_INDEX);
        if (key == null) {
            key = matcher.group(SHORT_NAME_MATCHER_INDEX);
        }
        return StringEscapeUtils.unescapeEcmaScript(key);
    }
}
