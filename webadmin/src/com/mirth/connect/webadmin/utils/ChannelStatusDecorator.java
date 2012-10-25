package com.mirth.connect.webadmin.utils;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

public class ChannelStatusDecorator implements DisplaytagColumnDecorator {
    @Override
    public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
        boolean status = (Boolean) columnValue;

        if (status) {
            return "Enabled";
        }
        return "Disabled";
    }
}
