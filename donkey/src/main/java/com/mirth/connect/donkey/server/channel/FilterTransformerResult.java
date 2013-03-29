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
