/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.filters.elements;

import com.mirth.connect.donkey.model.channel.MetaDataColumnType;

public enum MetaDataSearchOperator {
    EQUAL("="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    CONTAINS("CONTAINS"),
    STARTS_WITH("STARTS WITH"),
    ENDS_WITH("ENDS WITH");
    
    private String operator;

    private MetaDataSearchOperator(String operator) {
        this.operator = operator;
    }
    
    public String toString() {
        return operator;
    }
    
    public String toFullString() {
        return super.toString();
    }
    
    public static MetaDataSearchOperator[] valuesForColumnType(MetaDataColumnType type) {
        if (type == MetaDataColumnType.BOOLEAN) {
            return new MetaDataSearchOperator[]{EQUAL, NOT_EQUAL};
        }
        if (type == MetaDataColumnType.NUMBER) {
            return new MetaDataSearchOperator[]{EQUAL, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL};
        }
        if (type == MetaDataColumnType.STRING) {
            return new MetaDataSearchOperator[]{EQUAL, NOT_EQUAL, CONTAINS, STARTS_WITH, ENDS_WITH};
        }
        if (type == MetaDataColumnType.TIMESTAMP) {
            return new MetaDataSearchOperator[]{EQUAL, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL};
        }

        return null;
    }
    
    public static MetaDataSearchOperator fromString(String type) {
        if (type.equals("EQUAL")) {
            return EQUAL;
        } else if (type.equals("NOT_EQUAL")) {
            return NOT_EQUAL;
        } else if (type.equals("LESS_THAN")) {
            return LESS_THAN;
        } else if (type.equals("LESS_THAN_OR_EQUAL")) {
            return LESS_THAN_OR_EQUAL;
        } else if (type.equals("GREATER_THAN")) {
            return GREATER_THAN;
        } else if (type.equals("GREATER_THAN_OR_EQUAL")) {
            return GREATER_THAN_OR_EQUAL;
        } else if (type.equals("CONTAINS")) {
            return CONTAINS;
        } else if (type.equals("STARTS_WITH")) {
            return STARTS_WITH;
        } else if (type.equals("ENDS_WITH")) {
            return ENDS_WITH;
        } 
        
        return null;
    }
}
