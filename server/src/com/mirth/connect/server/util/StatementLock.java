package com.mirth.connect.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatementLock {
    private boolean vacuumLockRequired;
    private ReadWriteLock vacuumLock;
    private static Map<String, StatementLock> instances = new ConcurrentHashMap<>();

    private StatementLock(boolean lockRequired) {
        this.vacuumLockRequired = lockRequired;
        this.vacuumLock = lockRequired ? new ReentrantReadWriteLock(true) : null;
    }

    /**
     * Uses statementId to create a singleton used for locking methods
     * 
     * @param statementId
     * @return
     */
    public static StatementLock getInstance(String statementId) {
        StatementLock statementLock = instances.get(statementId);
        if (statementLock != null) {
            return statementLock;
        }

        synchronized (StatementLock.class) {
            statementLock = instances.get(statementId);
            if (statementLock != null) {
                return statementLock;
            }

            statementLock = new StatementLock(DatabaseUtil.statementExists(statementId));
            instances.put(statementId, statementLock);
            return statementLock;
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
