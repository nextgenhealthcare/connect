/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.passthru;

import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.server.data.jdbc.ConnectionPool;
import com.mirth.connect.donkey.util.SerializerProvider;

public class PassthruDaoFactory implements DonkeyDaoFactory {

    private StatisticsUpdater statisticsUpdater;

    public PassthruDaoFactory() {}

    public PassthruDaoFactory(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    @Override
    public void setEncryptData(boolean encryptData) {}

    @Override
    public void setDecryptData(boolean decryptData) {}

    @Override
    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    @Override
    public PassthruDao getDao() {
        PassthruDao dao = new PassthruDao();
        dao.setStatisticsUpdater(statisticsUpdater);
        return dao;
    }

    @Override
    public DonkeyDao getDao(SerializerProvider serializerProvider) {
        return getDao();
    }

    @Override
    public ConnectionPool getConnectionPool() {
        return null;
    }
}