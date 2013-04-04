/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

public class FilterTransformerResult {
    private boolean filtered;
    private String transformedContent;

    public FilterTransformerResult(boolean filtered, String transformedContent) {
        this.filtered = filtered;
        this.transformedContent = transformedContent;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public String getTransformedContent() {
        return transformedContent;
    }
}
