/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import java.util.LinkedList;
import java.util.Queue;

import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.Startable;
import com.mirth.connect.donkey.server.Stoppable;
import com.mirth.connect.donkey.server.data.DonkeyDao;

public class EventDatabaseWriter implements EventListener, Startable, Stoppable, Runnable {
    private static EventDatabaseWriter instance;

    public static EventDatabaseWriter getInstance() {
        synchronized (EventDatabaseWriter.class) {
            if (instance == null) {
                instance = new EventDatabaseWriter();
            }

            return instance;
        }
    }

    private Thread thread;
    private boolean running = false;
    private boolean forceStop = false;
    private Queue<Event> events = new LinkedList<Event>();

    private EventDatabaseWriter() {}

    @Override
    public void readEvent(Event event) {
        synchronized (events) {
            events.add(event);
        }
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            thread = new Thread(this);
            thread.setPriority(Constants.EVENT_HANDLER_THREAD_PRIORITY);
            thread.start();
        }
    }

    @Override
    public void stop() {
        if (running) {
            running = false;
            forceStop = false;

            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    @Override
    public void halt() {
        if (running) {
            running = false;
            forceStop = true;

            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(Constants.EVENT_HANDLER_WRITE_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                if (forceStop) {
                    return;
                }
            }

            flush();
        }
    }

    public void flush() {
        synchronized (events) {
            if (!events.isEmpty()) {
                Event event = null;
                DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

                try {
                    while (!events.isEmpty()) {
                        if (Thread.interrupted() && forceStop) {
                            return;
                        }

                        event = events.poll();

                        try {
                            dao.insertEvent(event);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                    dao.commit();
                } finally {
                    dao.close();
                }
            }
        }
    }
}
