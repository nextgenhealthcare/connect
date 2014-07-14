/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.purge.Purgable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("metaDataColumn")
public class MetaDataColumn implements Serializable, Purgable {
    private String name;
    private MetaDataColumnType type;
    private String mappingName;

    public MetaDataColumn() {

    }

    public MetaDataColumn(String name, MetaDataColumnType type, String mappingName) {
        this.name = name.toUpperCase();
        this.type = type;
        this.mappingName = mappingName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public MetaDataColumnType getType() {
        return type;
    }

    public void setType(MetaDataColumnType type) {
        this.type = type;
    }

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MetaDataColumn)) {
            return false;
        }

        MetaDataColumn metaDataColumn = (MetaDataColumn) object;
        if (metaDataColumn.getMappingName() != null && mappingName != null) {
            return (metaDataColumn.getName().equals(name) && metaDataColumn.getType() == type && metaDataColumn.getMappingName().equals(mappingName));
        } else if (metaDataColumn.getMappingName() == null && mappingName == null) {
            return (metaDataColumn.getName().equals(name) && metaDataColumn.getType() == type);
        } else {
            return false;
        }
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("type", type);
        return purgedProperties;
    }
}
