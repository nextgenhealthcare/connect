/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListRangeIterator {
    public static final int DEFAULT_LIST_LIMIT = 1000;

    private Iterator<Long> iterator;
    private int listSize;
    private boolean ascending;
    private Integer blockSize;
    private long lastId;
    private long startRange;
    private long endRange;
    private List<Long> list;
    private boolean listReady;
    private boolean rangeReady;
    private boolean finished;
    private int count;

    public ListRangeIterator(Iterator<Long> iterator, int listSize, boolean ascending, Integer blockSize) {
        this.iterator = iterator;
        this.listSize = listSize;
        this.ascending = ascending;
        this.blockSize = blockSize;
        list = new ArrayList<Long>(listSize);

        if (iterator.hasNext()) {
            lastId = iterator.next();
            startRange = lastId;
            endRange = lastId;
            count++;
        } else {
            finished = true;
        }
    }

    public boolean hasNext() {
        return !finished || listReady || rangeReady;
    }

    public ListRangeItem next() {
        ListRangeItem item = new ListRangeItem();

        List<Long> buffer = new ArrayList<Long>(listSize);

        // Iterate across all the Ids in the set
        while (!finished && !listReady && !rangeReady) {
            if (iterator.hasNext() && (blockSize == null || count++ < blockSize)) {
                // Retrieve the next Id
                long currentId = iterator.next();

                boolean contiguous = ascending ? (currentId == lastId + 1) : (currentId == lastId - 1);

                if (contiguous) {
                    // If the current Id is still contiguous, update the end range Id with the current Id
                    endRange = currentId;
                } else if (Math.abs(endRange - startRange) + 1 >= listSize) {
                    /*
                     * If the current contiguous block is greater than or equal to the max list
                     * size, then the next list and range are ready
                     */
                    listReady = true;
                    rangeReady = true;
                } else {
                    /*
                     * If the current contiguous block is less than the max list size, iterate
                     * across all the Ids from the start to the end of the contiguous block. Since
                     * this contiguous block will never be greater than the max list size, we need
                     * to add each of its values to the current list
                     */
                    for (long id = startRange; ascending ? (id <= endRange) : (id >= endRange); id += ascending ? 1 : -1) {
                        if (list.size() < listSize) {
                            // Add the Id to the current list until it is full
                            list.add(id);
                        } else {
                            /*
                             * Add any remaining Ids to the buffer. This buffer should never be
                             * greater than the max list size since the current contiguous block can
                             * never be greater than the max list size
                             */
                            buffer.add(id);
                        }
                    }

                    // The current list is ready if it is full
                    if (list.size() == listSize) {
                        listReady = true;
                    }

                    /*
                     * Since we've already distributed all the Ids in the contiguous block to the
                     * list or the buffer, we can reset the contiguous block to just the current Id
                     */
                    startRange = currentId;
                    endRange = currentId;
                }

                lastId = currentId;
            } else {
                // Once the end of the iterator is reached, always set the current list as ready
                listReady = true;

                if (startRange == lastId && endRange == lastId && list.size() < listSize) {
                    // If the last range is a single id, add it the current list if it is not full
                    list.add(lastId);
                } else {
                    // Otherwise indicate that there is a final range that is ready
                    rangeReady = true;
                }

                finished = true;
            }
        }

        if (listReady && !list.isEmpty()) {
            item.setList(new ArrayList<Long>(list));

            list.clear();

            /*
             * If there are Ids in the buffer then add them to the list and clear the buffer
             */
            if (!buffer.isEmpty()) {
                list.addAll(buffer);
                buffer.clear();
            }

            listReady = false;
        } else if (rangeReady) {
            item.setStartRange(startRange);
            item.setEndRange(endRange);

            /*
             * Since we've already pruned all the Ids in the contiguous block, we can reset the
             * contiguous block to just the current Id
             */
            startRange = lastId;
            endRange = lastId;

            listReady = false;
            rangeReady = false;
        }

        return item;
    }

    public class ListRangeItem {
        private List<Long> list;
        private Long startRange;
        private Long endRange;

        public List<Long> getList() {
            return list;
        }

        public void setList(List<Long> list) {
            this.list = list;
        }

        public Long getStartRange() {
            return startRange;
        }

        public void setStartRange(Long startRange) {
            this.startRange = startRange;
        }

        public Long getEndRange() {
            return endRange;
        }

        public void setEndRange(Long endRange) {
            this.endRange = endRange;
        }
    }
}
