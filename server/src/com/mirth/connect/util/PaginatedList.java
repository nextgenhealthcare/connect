package com.mirth.connect.util;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginatedList<T> extends ArrayList<T> {
    private int pageSize = 0;
    private int pageNumber = 1;
    private boolean hasNextPage = false;

    /**
     * Get the maximum number of items contained in a page
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Set the maximum number of items that can be contained in a page
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Get the current page number
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Get the total number of pages available in the current list
     * @return The total number of pages. Returns null if the total number of pages cannot be calculated.
     */
    public Integer getPageCount() {
        Long totalSize = getItemCount();

        if (totalSize != null) {
            return (int) Math.ceil((double) totalSize / (double) pageSize);
        } else {
            return null;
        }
    }
    
    /**
     * Get the item offset for the given page number
     */
    public int getOffset(int pageNumber) {
        return pageSize * (pageNumber - 1);
    }

    /**
     * Load items corresponding to the given page number into the list
     * @param pageNumber
     * @return TRUE if the page was loaded successfully and contains items, FALSE otherwise
     */
    public boolean loadPageNumber(int pageNumber) throws Exception {
        clear();
        hasNextPage = false;
        
        if (pageSize > 0) {
            // Retrieve one more item than pageSize so we know whether or not a next page exists
            List<T> items = getItems(getOffset(pageNumber), pageSize + 1);
            
            if (items != null && !items.isEmpty()) {
                if (items.size() > pageSize) {
                    hasNextPage = true;
                }
                
                // Add only items retrieved up to the pageSize
                for (int i = 0; i < Math.min(items.size(), pageSize); i++) {
                    add(items.get(i));
                }

                this.pageNumber = pageNumber;
                return true;
            }
        }
        
        return false;
    }
    
    public boolean hasNextPage() {
        return hasNextPage;
    }
    
    /**
     * Get the total number of items in the list
     * @return The total number of items in the list, or null if the total is unknown
     */
    public abstract Long getItemCount();

    /**
     * Get a list of items of type T using the given offset and limit
     */
    protected abstract List<T> getItems(int offset, int limit) throws Exception;
}