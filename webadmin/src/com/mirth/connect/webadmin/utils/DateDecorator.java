/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

public class DateDecorator implements DisplaytagColumnDecorator {
    private SimpleDateFormat simpleDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss:SSS");

    private String formatDate(Date date) {
        return simpleDate.format(date);
    }

    @Override
    public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
        Calendar cal = (Calendar) columnValue;
        return formatDate(cal.getTime());
    }
}
