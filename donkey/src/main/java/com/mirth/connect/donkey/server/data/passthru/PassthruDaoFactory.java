/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.passthru;

import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class PassthruDaoFactory implements DonkeyDaoFactory {
    private StatisticsUpdater statisticsUpdater;

    public PassthruDaoFactory() {}

    public PassthruDaoFactory(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    public StatisticsUpdater getStatisticsUpdater() {
        return statisticsUpdater;
    }

    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    @Override
    public void setEncryptData(boolean encryptData) {}

    @Override
    public void setDecryptData(boolean decryptData) {}

    @Override
    public PassthruDao getDao() {
        PassthruDao dao = new PassthruDao();

        if (statisticsUpdater != null) {
            dao.setStatisticsUpdater(statisticsUpdater);
        }

        return dao;
    }
}
