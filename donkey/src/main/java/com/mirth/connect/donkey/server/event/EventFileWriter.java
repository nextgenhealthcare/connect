/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.server.Startable;
import com.mirth.connect.donkey.server.Stoppable;

public class EventFileWriter implements EventListener, Startable, Stoppable, Runnable {
    private FileWriter writer;
    private Queue<Event> events = new LinkedList<Event>();
    private DateFormat dateFormatter;
    private boolean running;
    private Thread thread;
    private int bufferSize = 100;
    private long failedWrites = 0;

    public EventFileWriter() {
        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    public synchronized boolean setFile(File destination) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                return false;
            }
        }

        try {
            writer = new FileWriter(destination);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        running = false;

        synchronized (this) {
            notify();
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void halt() {
        running = false;

        thread.interrupt();

        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void readEvent(Event event) {
        events.add(event);

        if (events.size() > bufferSize) {
            flush();
        }
    }

    public synchronized void flush() {
        notify();
    }

    @Override
    public void run() {
        Event event;

        synchronized (this) {
            while (running) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    try {
                        writer.close();
                    } catch (IOException e1) {
                    }

                    return;
                }

                while (!events.isEmpty()) {
                    if (Thread.currentThread().isInterrupted()) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                        }

                        return;
                    }

                    event = events.poll();

                    try {
                        writer.append(String.format("%s     %-50s%-3d %-8d (%s)\n", dateFormatter.format(new Date(System.currentTimeMillis())), StringUtils.replaceChars(StringUtils.lowerCase(event.getEventType().toString()), '_', ' '), event.getMetaDataId(), event.getMessageId(), event.getMessageStatus()));
                    } catch (IOException e) {
                        failedWrites++;
                    }
                }
            }

            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }
}
