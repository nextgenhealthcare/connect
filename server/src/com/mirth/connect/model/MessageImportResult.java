/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

public class MessageImportResult {
    private int totalCount;
    private int successCount;

    public MessageImportResult(int totalCount, int successCount) {
        this.totalCount = totalCount;
        this.successCount = successCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }
}
