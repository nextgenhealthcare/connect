/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.client.core.ClientException;

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
    public boolean loadPageNumber(int pageNumber) throws ClientException {
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
            } else if (pageNumber > 1) {
                //Check the previous page if no items were found
                this.pageNumber = pageNumber - 1;
                return loadPageNumber(this.pageNumber);
            }
        }
        
        return false;
    }
    
    public boolean hasNextPage()
    {
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
    protected abstract List<T> getItems(int offset, int limit) throws ClientException;
}
