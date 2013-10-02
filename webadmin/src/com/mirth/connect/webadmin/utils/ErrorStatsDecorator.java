/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.utils;

import org.displaytag.decorator.TableDecorator;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.DashboardStatus;

public class ErrorStatsDecorator extends TableDecorator {
    @Override
    public String addRowClass() {
        long errorCount = ((DashboardStatus) getCurrentRowObject()).getStatistics().get(Status.ERROR);

        if (errorCount > 0) {
            return "bad";
        }
        return ("good");
    }
}
