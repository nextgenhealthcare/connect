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
