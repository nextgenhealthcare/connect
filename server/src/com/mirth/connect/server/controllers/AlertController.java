package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.server.alert.Alert;
import com.mirth.connect.server.alert.AlertWorker;

public abstract class AlertController extends Controller {
    public static AlertController getInstance() {
        return ControllerFactory.getFactory().createAlertController();
    }

    public abstract void initAlerts();

    public abstract void addWorker(AlertWorker alertWorker);

    public abstract void removeAllWorkers();

    public abstract List<AlertStatus> getAlertStatusList() throws ControllerException;

    public abstract AlertModel getAlert(String alertId) throws ControllerException;

    public abstract List<AlertModel> getAlerts() throws ControllerException;

    public abstract void updateAlert(AlertModel alert) throws ControllerException;

    public abstract void removeAlert(String alertId) throws ControllerException;

    public abstract void enableAlert(AlertModel alert) throws ControllerException;

    public abstract void disableAlert(AlertModel alert) throws ControllerException;

    public abstract Alert getEnabledAlert(String alertId);
}
