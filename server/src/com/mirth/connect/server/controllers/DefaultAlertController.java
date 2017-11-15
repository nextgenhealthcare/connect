/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.alert.Alert;
import com.mirth.connect.server.alert.AlertWorker;
import com.mirth.connect.server.alert.DefaultAlertWorker;
import com.mirth.connect.server.alert.action.ChannelProtocol;
import com.mirth.connect.server.alert.action.EmailProtocol;
import com.mirth.connect.server.alert.action.Protocol;
import com.mirth.connect.server.alert.action.UserProtocol;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.StatementLock;

public class DefaultAlertController extends AlertController {
    private static final String VACUUM_LOCK_ALERT_STATEMENT_ID = "Alert.vacuumAlertTable";
    
    private Logger logger = Logger.getLogger(this.getClass());

    private static AlertController instance = null;
    private static Map<Class<?>, AlertWorker> alertWorkers = new HashMap<Class<?>, AlertWorker>();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private Map<String, Protocol> alertActionProtocols = new LinkedHashMap<String, Protocol>();

    private DefaultAlertController() {
        addWorker(new DefaultAlertWorker());
        registerAlertActionProtocol(new EmailProtocol());
        registerAlertActionProtocol(new ChannelProtocol());
        registerAlertActionProtocol(new UserProtocol());
    }

    public static AlertController create() {
        synchronized (DefaultAlertController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(AlertController.class);

                if (instance == null) {
                    instance = new DefaultAlertController();
                }
            }

            return instance;
        }
    }

    @Override
    public void initAlerts() {
        try {
            List<AlertModel> alertModels = getAlerts();

            for (AlertModel alertModel : alertModels) {
                if (alertModel.isEnabled()) {
                    enableAlert(alertModel);
                }
            }
        } catch (ControllerException e) {
            logger.error("Failed to enable alerts on startup.", e);
        }
    }

    @Override
    public void addWorker(AlertWorker alertWorker) {
        alertWorkers.put(alertWorker.getTriggerClass(), alertWorker);

        eventController.addListener(alertWorker);
    }

    @Override
    public void removeAllWorkers() {
        for (AlertWorker worker : alertWorkers.values()) {
            eventController.removeListener(worker);
        }

        alertWorkers.clear();
    }

    @Override
    public List<AlertStatus> getAlertStatusList() throws ControllerException {
        List<AlertStatus> alertStatuses = new ArrayList<AlertStatus>();
        List<AlertModel> alertModels = getAlerts();

        for (AlertModel alertModel : alertModels) {
            AlertStatus alertStatus = getEnabledAlertStatus(alertModel.getId());

            if (alertStatus == null) {
                alertStatus = new AlertStatus();
            }

            alertStatus.setId(alertModel.getId());
            alertStatus.setName(alertModel.getName());
            alertStatus.setEnabled(alertModel.isEnabled());

            alertStatuses.add(alertStatus);
        }

        return alertStatuses;
    }

    private AlertStatus getEnabledAlertStatus(String alertId) {
        for (AlertWorker alertWorker : alertWorkers.values()) {
            AlertStatus alertStatus = alertWorker.getAlertStatus(alertId);

            if (alertStatus != null) {
                return alertStatus;
            }
        }

        return null;
    }

    @Override
    public AlertModel getAlert(String alertId) throws ControllerException {
        logger.debug("getting alert");

        StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).readLock();
        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            List<Map<String, Object>> rows = SqlConfig.getSqlSessionManager().selectList("Alert.getAlert", alertId);

            if (!rows.isEmpty()) {
                try {
                    return serializer.deserialize((String) rows.get(0).get("alert"), AlertModel.class);
                } catch (Exception e) {
                    logger.error("Failed to load alert " + alertId, e);
                }
            }

            return null;
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).readUnlock();
        }
    }

    @Override
    public List<AlertModel> getAlerts() throws ControllerException {
        logger.debug("getting alert");

        StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).readLock();
        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            List<Map<String, Object>> rows = SqlConfig.getSqlSessionManager().selectList("Alert.getAlert", null);
            List<AlertModel> alerts = new ArrayList<AlertModel>();

            for (Map<String, Object> row : rows) {
                try {
                    alerts.add(serializer.deserialize((String) row.get("alert"), AlertModel.class));
                } catch (Exception e) {
                    logger.warn("Failed to load alert " + row.get("id"), e);
                }
            }

            return alerts;
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).readUnlock();
        }
    }

    @Override
    public void updateAlert(AlertModel alert) throws ControllerException {
        if (alert == null) {
            return;
        }

        StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).readLock();
        try {
            if (alert.getName() != null) {
                Map<String, Object> params = new HashMap<String, Object>();

                params.put("id", alert.getId());
                params.put("name", alert.getName());

                if ((Boolean) SqlConfig.getSqlSessionManager().selectOne("Alert.getAlertNameExists", params)) {
                    throw new ControllerException("An alert with that name already exists.");
                }
            }
            
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

            boolean alertExists = CollectionUtils.isNotEmpty(SqlConfig.getSqlSessionManager().selectList("Alert.getAlert", alert.getId()));
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("id", alert.getId());
            params.put("name", alert.getName());
            params.put("alert", serializer.serialize(alert));

            if (alertExists) {
                disableAlert(alert.getId());

                logger.debug("updating alert");
                SqlConfig.getSqlSessionManager().update("Alert.updateAlert", params);
            } else {
                logger.debug("adding alert");
                SqlConfig.getSqlSessionManager().insert("Alert.insertAlert", params);
            }

            if (alert.isEnabled()) {
                enableAlert(alert);
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).readUnlock();
        }
    }

    @Override
    public void removeAlert(String alertId) throws ControllerException {
        logger.debug("removing alert");

        StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).writeLock();
        try {
            if (alertId != null) {
                disableAlert(alertId);

                // Delete the alert record from the "alert" table
                SqlConfig.getSqlSessionManager().delete("Alert.deleteAlert", alertId);

                if (DatabaseUtil.statementExists("Alert.vacuumAlertTable")) {
                    vacuumAlertTable();
                }
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_ALERT_STATEMENT_ID).writeUnlock();
        }
    }
    
    /**
     * When calling this method, a StatementLock writeLock should surround it
     */
    public void vacuumAlertTable() {
        SqlSession session = null;
        try {
            session = SqlConfig.getSqlSessionManager().openSession(false);
            if (DatabaseUtil.statementExists("Alert.lockAlertTable")) {
                session.update("Alert.lockAlertTable");
            }
            session.update("Alert.vacuumAlertTable");
            session.commit();
        } catch (Exception e) {
            logger.error("Could not compress Alert table", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void enableAlert(AlertModel alert) throws ControllerException {
        Class<?> clazz = alert.getTrigger().getClass();

        if (alertWorkers.containsKey(clazz)) {
            alertWorkers.get(clazz).enableAlert(alert);
        } else {
            logger.error("Failed to enable alert " + alert.getId() + ". Worker class for trigger " + clazz.getName() + " not found.");

            alert.setEnabled(false);
            updateAlert(alert);
        }
    }

    @Override
    public void disableAlert(String alertId) throws ControllerException {
        /*
         * Although we can look up the correct worker, we attempt to disable the alert on all
         * workers just in case any shenanigans have occurred.
         */
        for (AlertWorker worker : alertWorkers.values()) {
            worker.disableAlert(alertId);
        }
    }

    @Override
    public Alert getEnabledAlert(String alertId) {
        for (AlertWorker worker : alertWorkers.values()) {
            Alert alert = worker.getEnabledAlert(alertId);

            if (alert != null) {
                return alert;
            }
        }

        return null;
    }

    @Override
    public Protocol getAlertActionProtocol(String protocolName) {
        return alertActionProtocols.get(protocolName);
    }

    @Override
    public void registerAlertActionProtocol(Protocol protocol) {
        alertActionProtocols.put(protocol.getName(), protocol);
    }

    @Override
    public Map<String, Map<String, String>> getAlertActionProtocolOptions() {
        Map<String, Map<String, String>> options = new LinkedHashMap<String, Map<String, String>>();

        for (String protocol : alertActionProtocols.keySet()) {
            options.put(protocol, alertActionProtocols.get(protocol).getRecipientOptions());
        }

        return options;
    }
}
