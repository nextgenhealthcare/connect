package com.mirth.connect.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.builder.ToStringStyle;

public class MessageFilterToStringStyle extends ToStringStyle {
    public MessageFilterToStringStyle() {
        super();
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
        this.setContentStart("[\n");
        this.setFieldSeparator(",\n");
        this.setContentEnd("\n]");
    }
    
    public static MessageFilterToStringStyle instance() {
        return new MessageFilterToStringStyle();
    }
    
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (value instanceof Calendar) {
            value = String.format("%1$tY-%1$tm-%1$td", value);
        }

        buffer.append(value);
    }
    
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection<?> coll) {
        if (fieldName.equals("metaDataSearch")) {
            buffer.append("[\n");
            Iterator<?> iterator = coll.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!iterator.hasNext()) {
                    buffer.append(element.toString() + "\n");
                } else {
                    buffer.append(element.toString() + ",\n");
                }
            }
            buffer.append("]");
        }
    }
}
