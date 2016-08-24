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
import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.purge.Purgable;

public class ChannelPruningSettings implements Serializable, Purgable {

    private Integer pruneMetaDataDays;
    private Integer pruneContentDays;
    private boolean archiveEnabled;

    public ChannelPruningSettings() {
        archiveEnabled = true;
    }

    public Integer getPruneMetaDataDays() {
        return pruneMetaDataDays;
    }

    public void setPruneMetaDataDays(Integer pruneMetaDataDays) {
        this.pruneMetaDataDays = pruneMetaDataDays;
    }

    public Integer getPruneContentDays() {
        return pruneContentDays;
    }

    public void setPruneContentDays(Integer pruneContentDays) {
        this.pruneContentDays = pruneContentDays;
    }

    public boolean isArchiveEnabled() {
        return archiveEnabled;
    }

    public void setArchiveEnabled(boolean archiveEnabled) {
        this.archiveEnabled = archiveEnabled;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("pruneMetaDataDays", pruneMetaDataDays);
        purgedProperties.put("pruneContentDays", pruneContentDays);
        purgedProperties.put("archiveEnabled", archiveEnabled);
        return purgedProperties;
    }
}