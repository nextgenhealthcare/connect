package com.mirth.connect.donkey.util.purge;

import java.util.Map;

public interface Purgable {
    /**
     * Returns purged properties of this type as a map.
     */
    public Map<String, Object> getPurgedProperties();
}

