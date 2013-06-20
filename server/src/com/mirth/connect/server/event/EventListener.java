package com.mirth.connect.server.event;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.EventType;

public abstract class EventListener implements Runnable {

    private Thread workerThread = new Thread(this);
    protected BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();

    public EventListener() {
        workerThread.start();
    }

    public BlockingQueue<Event> getQueue() {
        return queue;
    }

    public void shutdown() {
        workerThread.interrupt();

        onShutdown();
    }

    protected abstract void onShutdown();

    public abstract Set<EventType> getEventTypes();

    protected abstract void processEvent(Event event);

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = queue.take();

                processEvent(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {

            }
        }
    }
}
