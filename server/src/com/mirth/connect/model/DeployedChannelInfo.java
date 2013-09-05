/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("deployedChannelInfo")
public class DeployedChannelInfo implements Serializable {

    private int deployedRevision;
    private Calendar deployedDate;

    public void setDeployedDate(Calendar deployedDate) {
        this.deployedDate = deployedDate;
    }

    public Calendar getDeployedDate() {
        return this.deployedDate;
    }

    public void setDeployedRevision(int deployedRevision) {
        this.deployedRevision = deployedRevision;
    }

    public int getDeployedRevision() {
        return this.deployedRevision;
    }
}
