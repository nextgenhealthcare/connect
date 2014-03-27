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

@XStreamAlias("channelHeader")
public class ChannelHeader implements Serializable {

    private int revision;
    private Calendar deployedDate;

    public ChannelHeader(int revision, Calendar deployedDate) {
        this.revision = revision;
        this.deployedDate = deployedDate;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public Calendar getDeployedDate() {
        return deployedDate;
    }

    public void setDeployedDate(Calendar deployedDate) {
        this.deployedDate = deployedDate;
    }
}
