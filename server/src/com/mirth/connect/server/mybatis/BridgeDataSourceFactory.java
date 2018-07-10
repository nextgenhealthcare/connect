/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.mybatis;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

import com.mirth.connect.donkey.server.Donkey;

public class BridgeDataSourceFactory extends UnpooledDataSourceFactory {

    public BridgeDataSourceFactory() {
        this.dataSource = Donkey.getInstance().getDaoFactory().getConnectionPool().getDataSource();
    }
}
