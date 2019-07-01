/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.regex.Pattern;

import com.google.common.base.Predicate;

/**
 * This predicate is used when scanning classes belonging to specific packages. Normally when using
 * Reflections to scan a package, the package name is used as a prefix. So if you're searching the
 * package "pkg.*", it will also match sub-packages like "pkg.v340.*". A regular expression is used
 * to ensure that sub-packages beginning with "v###" will not be matched.
 */
public class PackagePredicate implements Predicate<String> {

    private Pattern pattern;

    public PackagePredicate(String... packageNames) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < packageNames.length; i++) {
            builder.append(packageNames[i].replaceAll("\\.", "[\\\\./]")).append("(?![\\./]v\\d+).*");
            if (i < packageNames.length - 1) {
                builder.append('|');
            }
        }
        pattern = Pattern.compile(builder.toString());
    }

    @Override
    public boolean apply(String input) {
        return pattern.matcher(input).find();
    }
}