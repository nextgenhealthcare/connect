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

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.filters.SystemEventFilter;

public interface EventController {
    public void initialize();


    /**
     * Adds a new system event.
     * 
     * @param systemEvent
     * @throws ControllerException
     */
    public void logSystemEvent(SystemEvent systemEvent);

    /**
     * Clears the sysem event list.
     * 
     */
    public void clearSystemEvents() throws ControllerException;

    public int removeSystemEvents(SystemEventFilter filter) throws ControllerException;

    public int createSystemEventsTempTable(SystemEventFilter filter, String uid, boolean forceTemp) throws ControllerException;

    public void removeFilterTable(String uid);

    public void removeAllFilterTables();

    public List<SystemEvent> getSystemEventsByPage(int page, int pageSize, int maxSystemEvents, String uid) throws ControllerException;

    public List<SystemEvent> getSystemEventsByPageLimit(int page, int pageSize, int maxSystemEvents, String uid, SystemEventFilter filter) throws ControllerException;

}
