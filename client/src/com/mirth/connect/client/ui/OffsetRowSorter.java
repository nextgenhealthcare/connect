/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class OffsetRowSorter extends TableRowSorter<TableModel> {
    private int offset;

    public OffsetRowSorter(TableModel model, int offset) {
        super(model);
        this.offset = offset;
    }

    @Override
    public int convertRowIndexToView(int index) {
        // The first <offset> rows are always first in the view
        if (index < offset) {
            return index;
        }

        /*
         * Get the view (possibly sorted) index associated with the model index. Add one to it for
         * every static offset row that comes after it in the view. That way when the static rows
         * are placed at the beginning, any rows that would have been before it are bumped down the
         * line accordingly.
         */
        int currentViewIndex = super.convertRowIndexToView(index);
        int currentViewOffset = 0;
        for (int i = 0; i < offset; i++) {
            if (currentViewIndex < super.convertRowIndexToView(i)) {
                currentViewOffset++;
            }
        }

        // Return the actual view index plus the artificial offset
        return currentViewIndex + currentViewOffset;
    }

    @Override
    public int convertRowIndexToModel(int index) {
        // The first <offset> rows are always first in the model
        if (index < offset) {
            return index;
        }

        /*
         * Work backwards to find which model index would have been converted to the given view
         * index (including any artificial offset). Once we find it, return the associated model
         * index.
         */
        for (int i = offset; i < getModelRowCount(); i++) {
            if (index == convertRowIndexToView(i)) {
                return i;
            }
        }

        // If we did everything correctly, we should never get here
        return getModelRowCount() - 1;
    }
}