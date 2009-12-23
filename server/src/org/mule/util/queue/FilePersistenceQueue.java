package org.mule.util.queue;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilePersistenceQueue {

    private static final Log logger = LogFactory.getLog(FilePersistenceQueue.class);
    private long persistenceID = 1000;
    
    // Must use ConcurrentHashMap in this class to avoid infinite loops: http://lightbody.net/blog/2005/07/hashmapget_can_cause_an_infini.html
    private ConcurrentHashMap<String, ConcurrentHashMap<String, File>> queueMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, File>>();
    
    private FilePersistenceQueue() {

    }

    private static FilePersistenceQueue instance = null;

    public static FilePersistenceQueue getInstance() {
        if (instance == null) {
            instance = new FilePersistenceQueue();
        }
        return instance;
    }

    public synchronized void updateQueueId(long id) {
        if (persistenceID < id) {
            persistenceID = id;
        }
    }

    public synchronized void updateQueueId(String id) {
        try {
            updateQueueId(Long.parseLong(id));
        } catch (NumberFormatException e) {
            logger.error("Could not parse long: " + id, e);
        }
    }

    public synchronized long generateId() {
        return ++persistenceID;
    }

    public void clearQueueMap() {
        queueMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, File>>();
    }

    public void clearQueueMessageMap(String queue) {
        if (queueMap.containsKey(queue)) {
            queueMap.remove(queue);
        }
    }

    public void putInMessageMap(String queue, String id, File f) {
        if (!queueMap.containsKey(queue)) {
            queueMap.put(queue, new ConcurrentHashMap<String, File>());
        }
        queueMap.get(queue).put(id, f);
    }

    public File getFromMessageMap(String queue, String id) {
        if (!queueMap.containsKey(queue)) {
            return null;
        }
        return queueMap.get(queue).get(id);
    }

    public File peekFromMessageMap(String queue, String id) {
        ConcurrentHashMap<String, File> messageMap = queueMap.get(queue);
        if (messageMap == null) {
            return null;
        }
        File f = messageMap.get(id);
        if (f != null) {
            messageMap.remove(id);
        }
        return f;
    }

    public void removeFromMessageMap(String queue, String id) {
        if (!queueMap.containsKey(queue)) {
            return;
        }
        queueMap.get(queue).remove(id);
    }
}