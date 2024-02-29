package com.mirth.connect.server.alert;

import java.util.Map;
import java.util.concurrent.Callable;

import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.server.event.EventListener;

public abstract class AlertWorkerBase extends EventListener implements AlertActionAcceptor {

    public abstract void enableAlert(AlertModel alertModel);

    public abstract void disableAlert(String alertId);

    public abstract AlertStatus getAlertStatus(String alertId);

    public abstract Alert getEnabledAlert(String alertId);

    public abstract Class<?> getTriggerClass();

    protected abstract void alertEnabled(Alert alert);

    protected abstract void alertDisabled(Alert alert);

    protected abstract void triggerAction(Alert alert, Map<String, Object> context);
    
    protected abstract Void callActionTask(String alertId, AlertActionGroup actionGroup, Map<String, Object> context, long taskCreatedNanoTime) throws Exception;

    protected class ActionTask implements Callable<Void> {

        private String alertId;
        private AlertActionGroup actionGroup;
        private Map<String, Object> context;
        private long taskCreatedNanoTime;

        public ActionTask(String alertId, AlertActionGroup actionGroup, Map<String, Object> context) {
            this.alertId = alertId;
            this.actionGroup = actionGroup;
            this.context = context;
            this.taskCreatedNanoTime = System.nanoTime();
        }

        @Override
        public Void call() throws Exception {
            return callActionTask(alertId, actionGroup, context, taskCreatedNanoTime);
        }
    }
}
