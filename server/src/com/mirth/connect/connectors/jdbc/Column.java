/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.io.Serializable;
import java.sql.Types;

/**
 * Object to hold column information for a particular table.
 * 
 */
public class Column implements Serializable {
    private String name; // column name
    private String type; // SQL Type name, should follow @{link java.sql.Types}
    private int precision; // precision for the SQL type

    /**
     * @param name
     *            Column's name
     * @param type
     *            A SQL type name
     * @param precision
     *            Precision for the type (eg. the length for numeric, or characters for string)
     */
    public Column(String name, String type, int precision) {
        super();
        this.name = name;
        this.type = type;
        this.precision = precision;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The name of the SQL column type {@link Types}
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The SQL type name {@link Types}
     */
    public void setType(String type) {
        this.type = type;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Column))
            return false;

        Column col = (Column) obj;
        if (name != null && !name.equals(col.getName()))
            return false;
        if (type != null && !type.equals(col.getType()))
            return false;
        if (precision != col.getPrecision())
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (name == null ? 0 : name.hashCode());
        hashCode = 31 * hashCode + (type == null ? 0 : type.hashCode());
        hashCode = 31 * hashCode + (new Integer(precision)).hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName() + "[");
        builder.append("name=" + getName() + ", ");
        builder.append("type=" + getType() + ", ");
        builder.append("precision=" + getPrecision() + ", ");
        builder.append("]");
        return builder.toString();
    }
}
