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

@XStreamAlias("systemStats")
public class SystemStats implements Serializable {
    private Calendar timestamp;
    private double cpuUsagePct;
    private long allocatedMemoryBytes;
    private long freeMemoryBytes;
    private long maxMemoryBytes;
    private long diskFreeBytes;
    private long diskTotalBytes;

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public double getCpuUsagePct() {
        return cpuUsagePct;
    }

    public void setCpuUsagePct(double cpuUsagePct) {
        this.cpuUsagePct = cpuUsagePct;
    }

    public long getAllocatedMemoryBytes() {
        return allocatedMemoryBytes;
    }

    public void setAllocatedMemoryBytes(long allocatedMemoryBytes) {
        this.allocatedMemoryBytes = allocatedMemoryBytes;
    }

    public long getFreeMemoryBytes() {
        return freeMemoryBytes;
    }

    public void setFreeMemoryBytes(long freeMemoryBytes) {
        this.freeMemoryBytes = freeMemoryBytes;
    }

    public long getMaxMemoryBytes() {
        return maxMemoryBytes;
    }

    public void setMaxMemoryBytes(long maxMemoryBytes) {
        this.maxMemoryBytes = maxMemoryBytes;
    }

    public long getDiskFreeBytes() {
        return diskFreeBytes;
    }

    public void setDiskFreeBytes(long diskFreeBytes) {
        this.diskFreeBytes = diskFreeBytes;
    }

    public long getDiskTotalBytes() {
        return diskTotalBytes;
    }

    public void setDiskTotalBytes(long diskTotalBytes) {
        this.diskTotalBytes = diskTotalBytes;
    }
}
