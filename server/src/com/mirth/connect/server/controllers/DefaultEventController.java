/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
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

    public void addEvent(Event event) {
        logger.debug("adding event: " + event);

        try {
            SqlConfig.getSqlMapClient().insert("Event.insertEvent", event);
        } catch (Exception e) {
            logger.error("Error adding event.", e);
        }
    }

    public void removeAllEvents() throws ControllerException {
        logger.debug("removing all events");

        try {
            SqlConfig.getSqlMapClient().delete("Event.deleteEvent");

            if (DatabaseUtil.statementExists("Event.vacuumEventTable")) {
                SqlConfig.getSqlMapClient().update("Event.vacuumEventTable");
            }
        } catch (SQLException e) {
            throw new ControllerException("Error removing all events.", e);
        }
    }

    public String exportAndRemoveAllEvents() throws ControllerException {
        try {
            String exportFilePath = exportAllEvents();
            removeAllEvents();
            return exportFilePath;
        } catch (ControllerException e) {
            throw e;
        }
    }

    public String exportAllEvents() throws ControllerException {
        logger.debug("exporting events");

        long currentTimeMillis = System.currentTimeMillis();
        String currentTimeMillisString = String.valueOf(currentTimeMillis);
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(currentTimeMillis);
        String appDataDir = ControllerFactory.getFactory().createConfigurationController().getApplicationDataDir();
        File exportDir = new File(appDataDir, "exports");
        exportDir.mkdir();
        File exportFile = new File(exportDir, currentDateTime + "-events.txt");

        try {
            FileWriter writer = new FileWriter(exportFile, true);

            // write the CSV headers to the file
            writer.write(Event.getExportHeader());
            writer.write(System.getProperty("line.separator"));

            EventFilter filter = new EventFilter();
            int size = createTempTable(filter, currentTimeMillisString, true);
            int page = 0;
            int interval = 10;

            while ((page * interval) < size) {
                for (Event event : getEventsByPage(page, interval, size, currentTimeMillisString)) {
                    writer.write(event.toExportString());
                }

                page++;
            }

            IOUtils.closeQuietly(writer);
            logger.debug("events exported to file: " + exportFile.getAbsolutePath());
            removeFilterTable(currentDateTime);

            Event event = new Event("Sucessfully exported events");
            event.addAttribute("file", exportFile.getAbsolutePath());
            addEvent(event);
        } catch (IOException e) {
            throw new ControllerException("Error exporting events to file.", e);
        }

        return exportFile.getAbsolutePath();
    }

    private Map<String, Object> getEventFilterMap(EventFilter filter, String uid) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        if (uid != null) {
            parameterMap.put("uid", uid);
        }

        parameterMap.put("id", filter.getId());
        parameterMap.put("name", filter.getName());
        parameterMap.put("level", filter.getLevel());

        if (filter.getStartDate() != null) {
            parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", filter.getStartDate()));
        }

        if (filter.getEndDate() != null) {
            parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", filter.getEndDate()));
        }

        parameterMap.put("outcome", filter.getOutcome());
        parameterMap.put("userId", filter.getUserId());
        parameterMap.put("ipAddress", filter.getIpAddress());

        return parameterMap;
    }

    public int createTempTable(EventFilter filter, String uid, boolean forceTemp) throws ControllerException {
        logger.debug("creating temporary event table: filter=" + filter.toString());

        if (!forceTemp && DatabaseUtil.statementExists("Event.getEventsByPageLimit")) {
            return -1;
        }

        if (!forceTemp) {
            removeFilterTable(uid);
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

    public void removeFilterTable(String uid) {
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

    public List<Event> getEventsByPage(int page, int pageSize, int max, String uid) throws ControllerException {
        logger.debug("retrieving events by page: page=" + page);
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("uid", uid);

        if ((page != -1) && (pageSize != -1)) {
            int last = max - (page * pageSize);
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
