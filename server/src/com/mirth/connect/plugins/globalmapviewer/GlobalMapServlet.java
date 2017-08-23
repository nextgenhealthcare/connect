/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;

public class GlobalMapServlet extends MirthServlet implements GlobalMapServletInterface {

    private static final ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

    public GlobalMapServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public Map<String, String> getAllMaps(Set<String> channelIds, boolean includeGlobalMap) throws ClientException {
        try {
            Map<String, String> serializedMaps = new HashMap<String, String>();

            if (includeGlobalMap) {
                serializedMaps.put(null, MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalVariableStore.getInstance().getVariables())));
            }
            for (String channelId : channelIds) {
                serializedMaps.put(channelId, MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables())));
            }

            return serializedMaps;
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Map<String, String> getAllMapsPost(Set<String> channelIds, boolean includeGlobalMap) throws ClientException {
        return getAllMaps(channelIds, includeGlobalMap);
    }

    @Override
    public String getGlobalMap() throws ClientException {
        try {
            return MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalVariableStore.getInstance().getVariables()));
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public String getGlobalChannelMap(String channelId) throws ClientException {
        try {
            return MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables()));
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }
}