/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

public class DonkeyClonerFactory {
    public static DonkeyClonerFactory instance;

    public static DonkeyClonerFactory getInstance() {
        synchronized (DonkeyClonerFactory.class) {
            if (instance == null) {
                instance = new DonkeyClonerFactory();
            }

            return instance;
        }
    }

    public DonkeyCloner getCloner() {
//        return new SerializationUtilsCloner();
        return new FastCloner();
    }
}
