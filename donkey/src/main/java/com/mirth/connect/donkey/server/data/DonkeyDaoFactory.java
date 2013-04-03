/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data;

public interface DonkeyDaoFactory {
    /**
     * Get a DonkeyDao instance.
     */
    public DonkeyDao getDao();

    public void setEncryptData(boolean encryptData);

    public void setDecryptData(boolean decryptData);
}
