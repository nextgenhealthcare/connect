/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;

public class DefaultMetaData {

    public static final String SOURCE_COLUMN_NAME = "SOURCE";
    public static final String TYPE_COLUMN_NAME = "TYPE";
    public static final String VERSION_COLUMN_NAME = "VERSION";

    public static final MetaDataColumnType SOURCE_COLUMN_TYPE = MetaDataColumnType.STRING;
    public static final MetaDataColumnType TYPE_COLUMN_TYPE = MetaDataColumnType.STRING;
    public static final MetaDataColumnType VERSION_COLUMN_TYPE = MetaDataColumnType.STRING;

    public static final String SOURCE_VARIABLE_MAPPING = "mirth_source";
    public static final String TYPE_VARIABLE_MAPPING = "mirth_type";
    public static final String VERSION_VARIABLE_MAPPING = "mirth_version";

    public static final MetaDataColumn SOURCE_COLUMN = new MetaDataColumn(SOURCE_COLUMN_NAME, SOURCE_COLUMN_TYPE, SOURCE_VARIABLE_MAPPING);
    public static final MetaDataColumn TYPE_COLUMN = new MetaDataColumn(TYPE_COLUMN_NAME, TYPE_COLUMN_TYPE, TYPE_VARIABLE_MAPPING);
    public static final MetaDataColumn VERSION_COLUMN = new MetaDataColumn(VERSION_COLUMN_NAME, VERSION_COLUMN_TYPE, VERSION_VARIABLE_MAPPING);

    public static final List<MetaDataColumn> DEFAULT_COLUMNS;

    static {
        DEFAULT_COLUMNS = new ArrayList<MetaDataColumn>();
        DEFAULT_COLUMNS.add(SOURCE_COLUMN);
        DEFAULT_COLUMNS.add(TYPE_COLUMN);
    }
}