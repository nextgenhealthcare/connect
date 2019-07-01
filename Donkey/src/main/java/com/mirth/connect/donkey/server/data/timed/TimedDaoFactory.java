/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.timed;

import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.server.data.jdbc.ConnectionPool;
import com.mirth.connect.donkey.util.ActionTimer;
import com.mirth.connect.donkey.util.SerializerProvider;

public class TimedDaoFactory implements DonkeyDaoFactory {
    private DonkeyDaoFactory delegateFactory;
    private ActionTimer timer;
    private boolean encryptData = false;
    private boolean decryptData = true;
    private StatisticsUpdater statisticsUpdater;

    public TimedDaoFactory(DonkeyDaoFactory delegateFactory, ActionTimer timer) {
        this.delegateFactory = delegateFactory;
        this.timer = timer;
    }

    public DonkeyDaoFactory getDelegateFactory() {
        return delegateFactory;
    }

    public void setDelegateFactory(DonkeyDaoFactory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    public ActionTimer getTimer() {
        return timer;
    }

    public void setTimer(ActionTimer timer) {
        this.timer = timer;
    }

    @Override
    public void setEncryptData(boolean encryptData) {
        this.encryptData = encryptData;
    }

    @Override
    public void setDecryptData(boolean decryptData) {
        this.decryptData = decryptData;
    }

    @Override
    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    @Override
    public DonkeyDao getDao() {
        DonkeyDao dao = new TimedDao(delegateFactory.getDao(), timer);
        dao.setEncryptData(encryptData);
        dao.setDecryptData(decryptData);
        dao.setStatisticsUpdater(statisticsUpdater);
        return dao;
    }

    @Override
    public DonkeyDao getDao(SerializerProvider serializerProvider) {
        DonkeyDao dao = new TimedDao(delegateFactory.getDao(serializerProvider), timer);
        dao.setEncryptData(encryptData);
        dao.setDecryptData(decryptData);
        dao.setStatisticsUpdater(statisticsUpdater);
        return dao;
    }

    @Override
    public ConnectionPool getConnectionPool() {
        return delegateFactory.getConnectionPool();
    }
}
