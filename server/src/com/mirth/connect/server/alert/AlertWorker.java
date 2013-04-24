package com.mirth.connect.server.alert;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.server.event.EventListener;

public abstract class AlertWorker extends EventListener {

    protected Logger logger = Logger.getLogger(this.getClass());
    private ExecutorService workExecutor = Executors.newSingleThreadExecutor();
    protected ExecutorService actionExecutor = Executors.newSingleThreadExecutor();
    protected BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
    protected Map<String, Alert> enabledAlerts = new ConcurrentHashMap<String, Alert>();
    
    public AlertWorker() {
        startQueue();
    }
    
    public abstract Set<EventType> getEventTypes();
    
    public abstract Class<?> getTriggerClass();
    
    protected abstract Callable<?> getWorkerTask();
    
    protected abstract void triggerAction(Alert alert);

    public void enableAlert(AlertModel alertModel) {
        Alert alert = new Alert(alertModel);
        alert.getContext().put("alertId", alertModel.getId());
        alert.getContext().put("alertName", alertModel.getName());
        
        enabledAlerts.put(alertModel.getId(), alert);
    }

    public void disableAlert(AlertModel alertModel) {
        enabledAlerts.remove(alertModel.getId());
    }
    
    protected void startQueue() {
        workExecutor.submit(getWorkerTask());
    }

    protected void stopQueue() {
        workExecutor.shutdownNow();
    }

    @Override
    public BlockingQueue<Event> getQueue() {
        return queue;
    }

}
