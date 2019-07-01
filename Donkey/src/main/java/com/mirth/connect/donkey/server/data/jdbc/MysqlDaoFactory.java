/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import com.mirth.connect.donkey.util.SerializerProvider;

public class MysqlDaoFactory extends JdbcDaoFactory {
    @Override
    public JdbcDao getDao(SerializerProvider serializerProvider) {
        JdbcDao dao = super.getDao(serializerProvider);
        dao.setQuoteChar('`');
        return dao;
    }
}
