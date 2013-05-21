package com.mirth.connect.model.filters.elements;

import java.util.List;

public class ContentSearchElement {
    
    private int contentCode;
    private List<String> searches;

    public ContentSearchElement(int contentCode, List<String> searches) {
        this.contentCode = contentCode;
        this.searches = searches;
    }
    
    public int getContentCode() {
        return contentCode;
    }
    public void setContentCode(int contentCode) {
        this.contentCode = contentCode;
    }
    public List<String> getSearches() {
        return searches;
    }
    public void setSearches(List<String> searches) {
        this.searches = searches;
    }
}
