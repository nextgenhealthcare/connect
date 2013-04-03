/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.buffered;

import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class BufferedDaoFactory implements DonkeyDaoFactory {
    private DonkeyDaoFactory delegateFactory;
    private boolean encryptData = false;
    private boolean decryptData = true;

    public BufferedDaoFactory(DonkeyDaoFactory delegateFactory) {
        this.delegateFactory = delegateFactory;
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
    public DonkeyDao getDao() {
        return new BufferedDao(delegateFactory, encryptData, decryptData);
    }

}
