package com.mirth.connect.server.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.derby.agg.Aggregator;

/**
 * Implementation of a GROUP_CONCAT function similar to MySQL's. This was originally implemented due
 * to the fact that the message browser search query uses this type of feature from other DBs to
 * aggregate the metadata ids for each message found in the search. derby-database.sql contains the
 * sql statement that registers this function to make it visible to Derby.
 */
public class DerbyGroupConcat implements Aggregator<String, String, DerbyGroupConcat> {
    private List<String> strings;

    public List<String> getStrings() {
        return strings;
    }

    @Override
    public void init() {
        strings = new ArrayList<String>();
    }

    /**
     * Adds a value to the list of values to aggregate
     */
    @Override
    public void accumulate(String string) {
        strings.add(string);
    }

    /**
     * Merges another instance of this class with the current one
     */
    @Override
    public void merge(DerbyGroupConcat groupConcat) {
        strings.addAll(groupConcat.getStrings());
    }

    /**
     * Returns the final aggregate value
     */
    @Override
    public String terminate() {
        return StringUtils.join(strings, ',');
    }
}
