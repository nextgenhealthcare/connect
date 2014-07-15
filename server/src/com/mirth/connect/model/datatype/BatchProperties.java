/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.datatype;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BatchProperties extends DataTypePropertiesGroup {

    private boolean batchEnabled = false;
    private boolean lookAhead = false;
    private boolean useFirstResponse = false;

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("batchEnabled", new DataTypePropertyDescriptor(batchEnabled, "Enable Batch Processing", "Enable this option to process messages as batch.", PropertyEditorType.BOOLEAN));
        properties.put("lookAhead", new DataTypePropertyDescriptor(lookAhead, "Check End of Batch", "Enable this option to add an entry to the source map with key \"batchComplete\" that indicates if this is the last message in the batch. The connector will look ahead and cache the next message if it exists.", PropertyEditorType.BOOLEAN));
        properties.put("useFirstResponse", new DataTypePropertyDescriptor(useFirstResponse, "Use First Response", "Enable this option to respond with the response from the first message in the batch. Otherwise the response from the last message in the batch will be used.", PropertyEditorType.BOOLEAN));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("batchEnabled") != null) {
                batchEnabled = (Boolean) properties.get("batchEnabled");
            }

            if (properties.get("lookAhead") != null) {
                lookAhead = (Boolean) properties.get("lookAhead");
            }

            if (properties.get("useFirstResponse") != null) {
                useFirstResponse = (Boolean) properties.get("useFirstResponse");
            }
        }
    }

    public boolean isBatchEnabled() {
        return batchEnabled;
    }

    public void setBatchEnabled(boolean batchEnabled) {
        this.batchEnabled = batchEnabled;
    }

    public boolean isLookAhead() {
        return lookAhead;
    }

    public void setLookAhead(boolean lookAhead) {
        this.lookAhead = lookAhead;
    }

    public boolean isUseFirstResponse() {
        return useFirstResponse;
    }

    public void setUseFirstResponse(boolean useFirstResponse) {
        this.useFirstResponse = useFirstResponse;
    }
}
