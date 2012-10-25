/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import org.apache.commons.lang3.StringUtils;

public class EventPrinter implements EventListener {
    @Override
    public void readEvent(Event event) {
        System.out.printf("%-50s%-3d %-8d (%s)\n", StringUtils.replaceChars(StringUtils.lowerCase(event.getEventType().toString()), '_', ' '), event.getMetaDataId(), event.getMessageId(), event.getMessageStatus());
    }
}
