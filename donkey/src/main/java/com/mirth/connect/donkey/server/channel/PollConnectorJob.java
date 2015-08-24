package com.mirth.connect.donkey.server.channel;

import java.util.concurrent.locks.ReentrantLock;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

public class PollConnectorJob implements InterruptableJob {
    private PollConnector pollConnector;
    private Thread thread;
    private ReentrantLock lock;

    public PollConnectorJob(PollConnector pollConnector, ReentrantLock lock) {
        this.lock = lock;
        this.pollConnector = pollConnector;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	boolean locked = false;
    	if (!lock.isLocked()) {
    	    synchronized (lock) {
    	        if (!lock.isLocked()) {
    	            lock.lock();
    	            locked = true;
    	        }
    	    }
    	}
    	
    	if(locked) {
	        try {
	            if (!pollConnector.isTerminated()) {
	                thread = Thread.currentThread();
	
	                try {
	                    pollConnector.poll();
	                } catch (InterruptedException e) {
	                    Thread.currentThread().interrupt();
	                }
	            }
	        } finally {
	            lock.unlock();
	        }
    	}
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}