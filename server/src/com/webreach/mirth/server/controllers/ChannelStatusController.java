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

import java.util.List;

import com.webreach.mirth.model.ChannelStatus;

public abstract class ChannelStatusController extends Controller {
    public static ChannelStatusController getInstance() {
        return ControllerFactory.getFactory().createChannelStatusController();
    }
    
    public abstract void startChannel(String channelId) throws ControllerException;

    /**
     * Stops the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public abstract void stopChannel(String channelId) throws ControllerException;

    /**
     * Pauses the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public abstract void pauseChannel(String channelId) throws ControllerException;

    /**
     * Resumes the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public abstract void resumeChannel(String channelId) throws ControllerException;

    /**
     * Returns a list of ChannelStatus objects representing the running
     * channels.
     * 
     * @return
     * @throws ControllerException
     */
    public abstract List<ChannelStatus> getChannelStatusList();
}
