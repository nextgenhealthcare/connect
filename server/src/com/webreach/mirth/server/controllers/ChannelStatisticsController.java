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
public abstract class ChannelStatisticsController extends Controller {
    public static ChannelStatisticsController getInstance() {
        return ControllerFactory.getFactory().createChannelStatisticsController();
    }
    
    public abstract void shutdown();

    public abstract void start();
    
    public abstract void reloadLocalCache();

    public abstract ChannelStatistics getStatistics(String channelId);

    public abstract void createStatistics(String channelId);

    public abstract void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean errored, boolean alerted) throws ControllerException;

    public abstract boolean checkIfStatisticsExist(String channelId);

    public abstract void incrementReceivedCount(String channelId);

    public abstract void incrementSentCount(String channelId);

    public abstract void incrementFilteredCount(String channelId);

    public abstract void incrementErrorCount(String channelId);

    public abstract void incrementQueuedCount(String channelId);

    public abstract void incrementAlertedCount(String channelId);

    public abstract void decrementQueuedCount(String channelId);

    public abstract void decrementErrorCount(String channelId);

    public abstract void decrementFilteredCount(String channelId);

    public abstract void decrementSentCount(String channelId);

    public abstract void decrementReceivedCount(String channelId);

    public abstract void updateAllStatistics();
}