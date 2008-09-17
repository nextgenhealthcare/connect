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

import com.webreach.mirth.model.ChannelStatistics;

/**
 * The StatisticsContoller provides access to channel statistics.
 * 
 * @author GeraldB
 * 
 */
public interface ChannelStatisticsController extends Controller {
    public void shutdown();

    public void start();
    
    public void reloadLocalCache();

    public ChannelStatistics getStatistics(String channelId);

    public void createStatistics(String channelId);

    public void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean errored, boolean alerted) throws ControllerException;

    public boolean checkIfStatisticsExist(String channelId);

    public void incrementReceivedCount(String channelId);

    public void incrementSentCount(String channelId);

    public void incrementFilteredCount(String channelId);

    public void incrementErrorCount(String channelId);

    public void incrementQueuedCount(String channelId);

    public void incrementAlertedCount(String channelId);

    public void decrementQueuedCount(String channelId);

    public void decrementErrorCount(String channelId);

    public void decrementFilteredCount(String channelId);

    public void decrementSentCount(String channelId);

    public void decrementReceivedCount(String channelId);

    public void updateAllStatistics();
}