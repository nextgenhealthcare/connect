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
