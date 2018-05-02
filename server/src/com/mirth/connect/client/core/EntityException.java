/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.MirthXmlUtil;

public class EntityException extends Exception {

    private Object entity;

    public EntityException(Object entity) {
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public String getMessage() {
        if (entity instanceof String) {
            return (String) entity;
        } else if (entity != null) {
            try {
                StringBuilder builder = new StringBuilder();
                builder.append("Entity class: ").append(entity.getClass().getName()).append('\n');
                builder.append("---- Serialized Entity ----\n");
                builder.append(MirthXmlUtil.prettyPrint(ObjectXMLSerializer.getInstance().serialize(entity))).append('\n');
                builder.append("---------------------------\n");
                return builder.toString();
            } catch (Throwable t) {
            }
        }

        return "";
    }
}
