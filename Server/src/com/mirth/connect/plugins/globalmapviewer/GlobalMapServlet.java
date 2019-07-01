/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;

public class GlobalMapServlet extends MirthServlet implements GlobalMapServletInterface {

    private static final GlobalMapController globalMapController = GlobalMapController.getInstance();

    public GlobalMapServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public Map<String, Map<String, String>> getAllMaps(Set<String> channelIds, boolean includeGlobalMap) throws ClientException {
        try {
            return globalMapController.getAllMaps(channelIds, includeGlobalMap);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Map<String, Map<String, String>> getAllMapsPost(Set<String> channelIds, boolean includeGlobalMap) throws ClientException {
        return getAllMaps(channelIds, includeGlobalMap);
    }

    @Override
    public String getGlobalMap() throws ClientException {
        try {
            return globalMapController.getGlobalMap();
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public String getGlobalChannelMap(String channelId) throws ClientException {
        try {
            return globalMapController.getGlobalChannelMap(channelId);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }
}