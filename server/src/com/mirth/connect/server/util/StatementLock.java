package com.mirth.connect.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatementLock {
    private boolean vacuumLockRequired;
    private ReadWriteLock vacuumLock = new ReentrantReadWriteLock(true);
    private static Map<String, StatementLock> instances = new HashMap<>();
    
    private StatementLock(boolean lockRequired) {
        this.vacuumLockRequired = lockRequired;
    }
    
    /**
     * Uses statementId to create a singleton used for locking methods
     * @param statementId
     * @return
     */
    public static StatementLock getInstance(String statementId) {
        if (instances.containsKey(statementId)) {
            return instances.get(statementId);
        }
        
        synchronized (StatementLock.class) {
            if (instances.containsKey(statementId)) {
                return instances.get(statementId);
            }
            
            StatementLock instance = new StatementLock(DatabaseUtil.statementExists(statementId));
            instances.put(statementId, instance);
            return instance;
        }
    }
    
    public void readLock() {
        if (vacuumLockRequired) {
            vacuumLock.readLock().lock();
        }
    }
    
    public void readUnlock() {
        if (vacuumLockRequired) {
            vacuumLock.readLock().unlock();
        }
    }
    
    public void writeLock() {
        if (vacuumLockRequired) {
            vacuumLock.writeLock().lock();
        }
    }
    
    public void writeUnlock() {
        if (vacuumLockRequired) {
            vacuumLock.writeLock().unlock();
        }
    }
}
