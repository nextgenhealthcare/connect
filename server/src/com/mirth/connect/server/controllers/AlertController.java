package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.server.alert.AlertWorker;


public abstract class AlertController extends Controller {
    public static AlertController getInstance() {
        return ControllerFactory.getFactory().createAlertController();
    }
    
    public abstract void initAlerts();
    
    public abstract void addWorker(AlertWorker alertWorker);
    
    public abstract void removeAllWorkers();
    
    public abstract AlertModel getAlert(String alertId) throws ControllerException;
    
    public abstract List<AlertModel> getAlerts() throws ControllerException;
    
    public abstract void updateAlert(AlertModel alert) throws ControllerException;
    
    public abstract void removeAlert(String alertId) throws ControllerException;
    
    public abstract void enableAlert(AlertModel alert) throws ControllerException;
    
    public abstract void disableAlert(AlertModel alert) throws ControllerException;
}
