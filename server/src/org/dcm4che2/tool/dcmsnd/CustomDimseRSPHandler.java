/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package org.dcm4che2.tool.dcmsnd;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.Association;

public abstract class CustomDimseRSPHandler {

    public abstract void onDimseRSP(Association as, DicomObject cmd, DicomObject data);
}
