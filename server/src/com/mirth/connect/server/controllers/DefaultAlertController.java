/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.alert.AlertWorker;
import com.mirth.connect.server.alert.DefaultAlertWorker;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultAlertController extends AlertController {
    private Logger logger = Logger.getLogger(this.getClass());

    private static DefaultAlertController instance = null;
    private static Map<Class<?>, AlertWorker> alertWorkers = new HashMap<Class<?>, AlertWorker>();
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    private DefaultAlertController() {
        addWorker(new DefaultAlertWorker());
    }

    public static AlertController create() {
        synchronized (DefaultAlertController.class) {
            if (instance == null) {
                instance = new DefaultAlertController();
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
        
        eventController.addListener(alertWorker, alertWorker.getEventTypes());
    }
    
    @Override
    public void removeAllWorkers() {
        for (AlertWorker worker : alertWorkers.values()) {
            eventController.removeListener(worker);
        }
        
        alertWorkers.clear();
    }
    
    @Override
    public AlertModel getAlert(String alertId) throws ControllerException {
        logger.debug("getting alert");

        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            List<Map<String, Object>> rows = SqlConfig.getSqlSessionManager().selectList("Alert.getAlert", alertId);
            
            AlertModel alertModel = null;
            
            if (!rows.isEmpty()) {
                alertModel = (AlertModel) serializer.deserialize((String) rows.get(0).get("alert"));
            }

            return alertModel;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public List<AlertModel> getAlerts() throws ControllerException {
        logger.debug("getting alert");

        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            List<Map<String, Object>> rows = SqlConfig.getSqlSessionManager().selectList("Alert.getAlert", null);
            List<AlertModel> alerts = new ArrayList<AlertModel>();
            
            for (Map<String, Object> row : rows) {
                alerts.add((AlertModel) serializer.deserialize((String) row.get("alert")));
            }

            return alerts;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public void updateAlert(AlertModel alert) throws ControllerException {
        if (alert == null) {
            return;
        }
        
        if (alert.getName() != null) {
            Map<String, Object> params = new HashMap<String, Object>();
            
            params.put("id", alert.getId());
            params.put("name", alert.getName());
            
            if ((Boolean) SqlConfig.getSqlSessionManager().selectOne("Alert.getAlertNameExists", params)) {
                throw new ControllerException("An alert with that name aleady exists.");
            }
        }
        
        AlertModel matchingAlert = getAlert(alert.getId());

        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            Map<String, Object> params = new HashMap<String, Object>();
            
            params.put("id", alert.getId());
            params.put("name", alert.getName());
            params.put("alert", serializer.serialize(alert));
            
            if (matchingAlert != null) {
                disableAlert(matchingAlert);

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
        }
    }

    @Override
    public void removeAlert(String alertId) throws ControllerException {
        logger.debug("removing alert");

        try {
            if (alertId != null) {
                AlertModel matchingAlert = getAlert(alertId);

                if (matchingAlert != null) {
                    disableAlert(matchingAlert);
                }

                // Delete the alert record from the "alert" table
                SqlConfig.getSqlSessionManager().delete("Alert.deleteAlert", alertId);

                if (DatabaseUtil.statementExists("Alert.vacuumAlertTable")) {
                    SqlConfig.getSqlSessionManager().update("Alert.vacuumAlertTable");
                }
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public void enableAlert(AlertModel alert) throws ControllerException {
        Class<?> clazz = alert.getTrigger().getClass();

        if (alertWorkers.containsKey(clazz)) {
            alertWorkers.get(clazz).enableAlert(alert);
        } else {
            logger.error("Failed to enable alert " + alert.getId() + ". Worker class " + clazz.getName() + " not found.");
            
            alert.setEnabled(false);
            updateAlert(alert);
        }
    }

    @Override
    public void disableAlert(AlertModel alert) throws ControllerException {
        /*
         * Although we can look up the correct worker, we attempt to disable the alert on all
         * workers just in case any shenanigans have occurred.
         */
        for (AlertWorker worker : alertWorkers.values()) {
            worker.disableAlert(alert);
        }
    }

}
