/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Comparator;

public class NumberCellComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }

}