/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util.purge;

import java.util.Map;

public interface Purgable {
    /**
     * Returns purged properties of this type as a map.
     */
    public Map<String, Object> getPurgedProperties();
    
    public default Map<String, Object> getPurgedProperties(PurgeHelper helper) {
        return getPurgedProperties();
    }
}
