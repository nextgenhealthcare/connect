/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelSummary;

public interface ChannelController extends Controller {
    public void initialize();

    public List<Channel> getChannel(Channel channel) throws ControllerException;

    public List<Channel> getEnabledChannels() throws ControllerException;

    public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException;

    public boolean updateChannel(Channel channel, boolean override) throws ControllerException;

    public void removeChannel(Channel channel) throws ControllerException;

    // channel cache
    public HashMap<String, Channel> getChannelCache();

    public void setChannelCache(HashMap<String, Channel> channelCache);

    public void refreshChannelCache(List<Channel> channels) throws ControllerException;

    // utility methods
    public String getChannelId(String channelName);

    public String getDestinationName(String id);

    public String getConnectorId(String channelId, String connectorName) throws Exception;
}
