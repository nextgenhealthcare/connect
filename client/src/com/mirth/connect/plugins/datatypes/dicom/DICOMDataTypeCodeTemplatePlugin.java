/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.dicom;

import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.plugins.DataTypeCodeTemplatePlugin;

public class DICOMDataTypeCodeTemplatePlugin extends DataTypeCodeTemplatePlugin {

    public DICOMDataTypeCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return new DICOMDataTypeDelegate();
    }

    @Override
    protected String getDisplayName() {
        return "DICOM";
    }

    @Override
    protected boolean isDefaultOnly() {
        return true;
    }
}