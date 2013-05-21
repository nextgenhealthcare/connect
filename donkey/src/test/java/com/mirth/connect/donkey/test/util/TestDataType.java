/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import com.mirth.connect.donkey.server.message.DataType;

public class TestDataType extends DataType {
    public TestDataType() {
        super("HL7V2", new TestSerializer(), null, new TestAutoResponder(), new TestResponseValidator());
    }
}
