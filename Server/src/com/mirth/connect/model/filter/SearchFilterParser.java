/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.model.ChannelTag;

public class SearchFilterParser {

    private static final String DEFAULT_FILTER_KEY = "name";
    private static final String NAME_FILTER_KEY = "channel";
    private static final String TAG_FILTER_KEY = "tag";

    public static List<SearchFilter> parse(String filter, Set<ChannelTag> channelTags) {
        List<SearchFilter> searchFilters = new ArrayList<SearchFilter>();

        if (StringUtils.isNotBlank(filter)) {
            Map<String, List<String>> filterMap = new HashMap<String, List<String>>();

            for (String filterEntry : filter.split(",")) {
                String[] filterPair = filterEntry.split(":");

                if (ArrayUtils.isNotEmpty(filterPair) && filterPair.length == 2) {
                    String filterType = filterPair[0].trim().replace("\"", "");
                    String filterName = filterPair[1].trim().replace("\"", "");

                    List<String> list = filterMap.get(filterType);
                    if (list == null) {
                        list = new ArrayList<String>();
                        filterMap.put(filterType, list);
                    }
                    list.add(filterName);
                }
            }

            for (Entry<String, List<String>> entry : filterMap.entrySet()) {
                String filterType = entry.getKey();
                if (StringUtils.equalsIgnoreCase(filterType, NAME_FILTER_KEY)) {
                    searchFilters.add(new ChannelNameSearchFilter(entry.getValue(), false));
                } else if (StringUtils.equalsIgnoreCase(filterType, TAG_FILTER_KEY)) {
                    searchFilters.add(new ChannelTagSearchFilter(channelTags, entry.getValue()));
                } else if (StringUtils.equalsIgnoreCase(filterType, DEFAULT_FILTER_KEY)) {
                    searchFilters.add(new ChannelNameSearchFilter(entry.getValue(), true));
                }
            }
        }

        return searchFilters;
    }
}
