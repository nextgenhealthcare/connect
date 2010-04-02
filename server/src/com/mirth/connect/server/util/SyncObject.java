package com.mirth.connect.server.util;

import java.util.concurrent.locks.ReentrantLock;

public class SyncObject {
    private Object value;
    private ReentrantLock lock;

    public SyncObject(Object value, ReentrantLock lock) {
        this.setValue(value);
        this.setLock(lock);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
