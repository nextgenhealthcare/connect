/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class CommandQueue {
    private PriorityBlockingQueue<Command> commandQueue = new PriorityBlockingQueue<Command>();

    // singleton pattern
    private static CommandQueue instance = null;

    private CommandQueue() {}

    public static CommandQueue getInstance() {
        synchronized (CommandQueue.class) {
            if (instance == null)
                instance = new CommandQueue();

            return instance;
        }
    }

    /**
     * Adds a <code>MirthCommand</code> to the command queue.
     * 
     * @param command
     */
    public void addCommand(Command command) {
        commandQueue.put(command);
    }

    /**
     * Returns the command with the highest priority from the queue.
     * 
     * @return the command with the highest priority from the queue.
     */
    public Command getCommand() {
        try {
            return commandQueue.take();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Prints the contents of the command queue.
     * 
     */
    public void printQueue() {
        for (Iterator<Command> iter = commandQueue.iterator(); iter.hasNext();) {
            System.out.println(iter.next());
        }
    }

    /**
     * Clears the contents of the command queue.
     * 
     */
    public void clear() {
        commandQueue.clear();
    }
}
