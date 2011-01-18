/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Event;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultEventController extends EventController {
    private Logger logger = Logger.getLogger(this.getClass());
    private static DefaultEventController instance = null;

    private DefaultEventController() {

    }

    public static EventController create() {
        synchronized (DefaultEventController.class) {
            if (instance == null) {
                instance = new DefaultEventController();
            }

            return instance;
        }
    }

    public void removeAllEventFilterTables() {
        Connection conn = null;
        ResultSet resultSet = null;

        try {
            conn = SqlConfig.getSqlMapClient().getDataSource().getConnection();
            // Gets the database metadata
            DatabaseMetaData dbmd = conn.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = { "TABLE" };
            String tablePattern = "EVT_TMP_%";
            resultSet = dbmd.getTables(null, null, tablePattern, types);

            boolean resultFound = resultSet.next();

            // Some databases only accept lowercase table names
            if (!resultFound) {
                resultSet = dbmd.getTables(null, null, tablePattern.toLowerCase(), types);
                resultFound = resultSet.next();
            }

            while (resultFound) {
                // Get the table name
                String tableName = resultSet.getString(3);
                // Get the uid and remove its filter tables/indexes/sequences
                removeEventFilterTable(tableName.substring(8));
                resultFound = resultSet.next();
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
        }
    }

    public void addEvent(Event event) {
        logger.debug("adding event: " + event);

        try {
            SqlConfig.getSqlMapClient().insert("Event.insertEvent", event);
        } catch (Exception e) {
            logger.error("Could not add event.", e);
        }
    }

    public void clearEvents() throws ControllerException {
        logger.debug("removing all events");

        try {
            SqlConfig.getSqlMapClient().delete("Event.deleteEvent");

            if (DatabaseUtil.statementExists("Event.vacuumEventTable")) {
                SqlConfig.getSqlMapClient().update("Event.vacuumEventTable");
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    private Map<String, Object> getEventFilterMap(EventFilter filter, String uid) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        if (uid != null) {
            parameterMap.put("uid", uid);
        }

        parameterMap.put("id", filter.getId());
        parameterMap.put("event", filter.getEvent());
        parameterMap.put("level", filter.getLevel());

        if (filter.getStartDate() != null) {
            parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate()));
        }

        if (filter.getEndDate() != null) {
            parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate()));
        }

        return parameterMap;
    }

    public int createEventTempTable(EventFilter filter, String uid, boolean forceTemp) throws ControllerException {
        logger.debug("creating temporary event table: filter=" + filter.toString());

        if (!forceTemp && DatabaseUtil.statementExists("Event.getEventsByPageLimit")) {
            return -1;
        }
        
        if (!forceTemp) {
            removeEventFilterTable(uid);
        }

        try {
            if (DatabaseUtil.statementExists("Event.createTempEventTableSequence")) {
                SqlConfig.getSqlMapClient().update("Event.createTempEventTableSequence", uid);
            }

            SqlConfig.getSqlMapClient().update("Event.createTempEventTable", uid);
            SqlConfig.getSqlMapClient().update("Event.createTempEventTableIndex", uid);
            return SqlConfig.getSqlMapClient().update("Event.populateTempEventTable", getEventFilterMap(filter, uid));
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeEventFilterTable(String uid) {
        logger.debug("removing temporary event table: uid=" + uid);
        
        try {
            if (DatabaseUtil.statementExists("Event.dropTempEventTableSequence")) {
                SqlConfig.getSqlMapClient().update("Event.dropTempEventTableSequence", uid);
            }
        } catch (SQLException e) {
            logger.debug(e);
        }
        
        try {
            if (DatabaseUtil.statementExists("Event.deleteTempEventTableIndex")) {
                SqlConfig.getSqlMapClient().update("Event.deleteTempEventTableIndex", uid);
            }
        } catch (SQLException e) {
            logger.debug(e);
        }
        
        try {
            SqlConfig.getSqlMapClient().update("Event.dropTempEventTable", uid);
        } catch (SQLException e) {
            logger.debug(e);
        }
    }

    public List<Event> getEventsByPage(int page, int pageSize, int maxEvents, String uid) throws ControllerException {
        logger.debug("retrieving events by page: page=" + page);
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("uid", uid);

        if ((page != -1) && (pageSize != -1)) {
            int last = maxEvents - (page * pageSize);
            int first = last - pageSize + 1;
            parameterMap.put("first", first);
            parameterMap.put("last", last);
        }

        try {
            return SqlConfig.getSqlMapClient().queryForList("Event.getEventsByPage", parameterMap);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public List<Event> getEventsByPageLimit(int page, int pageSize, int maxEvents, String uid, EventFilter filter) throws ControllerException {
        logger.debug("retrieving events by page: page=" + page);
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("uid", uid);
        int offset = page * pageSize;
        parameterMap.put("offset", offset);
        parameterMap.put("limit", pageSize);
        parameterMap.putAll(getEventFilterMap(filter, uid));

        try {
            return SqlConfig.getSqlMapClient().queryForList("Event.getEventsByPageLimit", parameterMap);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
}
