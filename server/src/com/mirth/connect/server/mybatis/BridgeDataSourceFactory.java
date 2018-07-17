/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.mybatis;

import java.util.Properties;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class BridgeDataSourceFactory extends UnpooledDataSourceFactory {

    @Override
    public void setProperties(Properties properties) {
        DonkeyDaoFactory daoFactory;
        if (Boolean.parseBoolean(properties.getProperty("readonly"))) {
            daoFactory = Donkey.getInstance().getReadOnlyDaoFactory();
        } else {
            daoFactory = Donkey.getInstance().getDaoFactory();
        }

        this.dataSource = daoFactory.getConnectionPool().getDataSource();
    }
}
