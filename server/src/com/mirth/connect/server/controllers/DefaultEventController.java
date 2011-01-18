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

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.mirth.connect.model.SystemEvent;
import com.mirth.connect.model.filters.SystemEventFilter;
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

    public void removeAllFilterTables() {
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
                removeFilterTable(tableName.substring(8));
                resultFound = resultSet.next();
            }

        } catch (SQLException e) {
            logger.error(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
        }
    }

    public void logSystemEvent(SystemEvent systemEvent) {
        logger.debug("adding log event: " + systemEvent);

        try {
            SqlConfig.getSqlMapClient().insert("Event.insertEvent", systemEvent);
        } catch (Exception e) {
            logger.error("could not log system event", e);
        }
    }

    public void clearSystemEvents() throws ControllerException {
        logger.debug("clearing system event list");

        try {
            SqlConfig.getSqlMapClient().delete("Event.deleteEvent");

            if (DatabaseUtil.statementExists("Event.vacuumEventTable")) {
                SqlConfig.getSqlMapClient().update("Event.vacuumEventTable");
            }

        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    private Map<String, Object> getFilterMap(SystemEventFilter filter, String uid) {
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

    public int createSystemEventsTempTable(SystemEventFilter filter, String uid, boolean forceTemp) throws ControllerException {
        logger.debug("creating temporary system event table: filter=" + filter.toString());

        if (!forceTemp && statementExists("Event.getSystemEventsByPageLimit")) {
            return -1;
        }
        // If it's not forcing temp tables (export or reprocessing),
        // then it's reusing the same ones, so remove them.
        if (!forceTemp) {
            removeFilterTable(uid);
        }

        try {
            if (statementExists("Event.createTempSystemEventsTableSequence")) {
                SqlConfig.getSqlMapClient().update("Event.createTempSystemEventsTableSequence", uid);
            }

            SqlConfig.getSqlMapClient().update("Event.createTempSystemEventsTable", uid);
            SqlConfig.getSqlMapClient().update("Event.createTempSystemEventsTableIndex", uid);
            return SqlConfig.getSqlMapClient().update("Event.populateTempSystemEventsTable", getFilterMap(filter, uid));
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeFilterTable(String uid) {
        logger.debug("Removing temporary system event table: uid=" + uid);
        try {
            if (statementExists("Event.dropTempSystemEventsTableSequence")) {
                SqlConfig.getSqlMapClient().update("Event.dropTempSystemEventsTableSequence", uid);
            }
        } catch (SQLException e) {
            // supress any warnings about the sequence not existing
            logger.debug(e);
        }
        try {
            if (statementExists("Event.deleteTempSystemEventsTableIndex")) {
                SqlConfig.getSqlMapClient().update("Event.deleteTempSystemEventsTableIndex", uid);
            }
        } catch (SQLException e) {
            // supress any warnings about the index not existing
            logger.debug(e);
        }
        try {
            SqlConfig.getSqlMapClient().update("Event.dropTempSystemEventsTable", uid);
        } catch (SQLException e) {
            // supress any warnings about the table not existing
            logger.debug(e);
        }
    }

    public List<SystemEvent> getSystemEventsByPage(int page, int pageSize, int maxSystemEvents, String uid) throws ControllerException {
        logger.debug("retrieving system events by page: page=" + page);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("uid", uid);

            if ((page != -1) && (pageSize != -1)) {
                int last = maxSystemEvents - (page * pageSize);
                int first = last - pageSize + 1;
                parameterMap.put("first", first);
                parameterMap.put("last", last);
            }

            List<SystemEvent> systemEvents = SqlConfig.getSqlMapClient().queryForList("Event.getSystemEventsByPage", parameterMap);

            return systemEvents;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public int removeSystemEvents(SystemEventFilter filter) throws ControllerException {
        logger.debug("removing system events: filter=" + filter.toString());

        try {
            int eventsDeleted = SqlConfig.getSqlMapClient().delete("Event.deleteEvent", getFilterMap(filter, null));

            if (DatabaseUtil.statementExists("Event.vacuumEventTable")) {
                SqlConfig.getSqlMapClient().update("Event.vacuumEventTable");
            }

            return eventsDeleted;
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public List<SystemEvent> getSystemEventsByPageLimit(int page, int pageSize, int maxSystemEvents, String uid, SystemEventFilter filter) throws ControllerException {
        logger.debug("retrieving system events by page: page=" + page);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("uid", uid);
            int offset = page * pageSize;

            parameterMap.put("offset", offset);
            parameterMap.put("limit", pageSize);

            parameterMap.putAll(getFilterMap(filter, uid));

            List<SystemEvent> systemEvents = SqlConfig.getSqlMapClient().queryForList("Event.getSystemEventsByPageLimit", parameterMap);

            return systemEvents;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private boolean statementExists(String statement) {
        try {
            SqlMapExecutorDelegate delegate = ((SqlMapClientImpl) SqlConfig.getSqlMapClient()).getDelegate();
            delegate.getMappedStatement(statement);
        } catch (SqlMapException sme) {
            // The statement does not exist
            return false;
        }

        return true;
    }
}
