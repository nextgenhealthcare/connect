package com.mirth.connect.client.core;

import java.util.ArrayList;
import java.util.List;

public class MessageImportResult {
    private int total = 0;
    private List<Long> erroredMessageIds = new ArrayList<Long>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
    
    public void incrementTotal() {
        total++;
    }

    public List<Long> getErroredMessageIds() {
        return erroredMessageIds;
    }

    public void setErroredMessageIds(List<Long> erroredMessageIds) {
        this.erroredMessageIds = erroredMessageIds;
    }
}
