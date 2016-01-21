/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.buffered;

import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.util.SerializerProvider;

public class BufferedDaoFactory implements DonkeyDaoFactory {
    private DonkeyDaoFactory delegateFactory;
    private SerializerProvider serializerProvider;
    private boolean encryptData = false;
    private boolean decryptData = true;
    private StatisticsUpdater statisticsUpdater;

    public BufferedDaoFactory(DonkeyDaoFactory delegateFactory, SerializerProvider serializerProvider, StatisticsUpdater statisticsUpdater) {
        this.delegateFactory = delegateFactory;
        this.serializerProvider = serializerProvider;
        this.statisticsUpdater = statisticsUpdater;
    }

    public DonkeyDaoFactory getDelegateFactory() {
        return delegateFactory;
    }

    public void setDelegateFactory(DonkeyDaoFactory delegateFactory) {
        this.delegateFactory = delegateFactory;
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
        return getDao(serializerProvider);
    }

    @Override
    public DonkeyDao getDao(SerializerProvider serializerProvider) {
        return new BufferedDao(delegateFactory, serializerProvider, encryptData, decryptData, statisticsUpdater);
    }
}
