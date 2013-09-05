/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

/**
 * A command for the Mirth service. Consists of a command, a parameter, and a
 * priority. Default priority of a command is normal.
 * 
 */
public class Command implements Comparable<Command> {
    public enum Operation {
        START_SERVER, SHUTDOWN_SERVER
    }

    public enum Priority {
        NORMAL(0), HIGH(1);

        private final int value;

        private Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Operation operation;
    private Priority priority;
    private Object parameter;

    public Command(Operation operation) {
        this(operation, null, Priority.NORMAL);
    }

    public Command(Operation operation, Object parameter) {
        this(operation, parameter, Priority.NORMAL);
    }

    public Command(Operation operation, Priority priority) {
        this(operation, null, priority);
    }

    public Command(Operation operation, Object parameter, Priority priority) {
        this.operation = operation;
        this.parameter = parameter;
        this.priority = priority;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Object getParameter() {
        return parameter;
    }

    public void setParameter(Object parameter) {
        this.parameter = parameter;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int compareTo(Command compareCommand) {
        if (getPriority().getValue() < compareCommand.getPriority().getValue())
            return -1;
        else if (getPriority().getValue() > compareCommand.getPriority().getValue())
            return 1;
        else
            return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Command[");
        builder.append("operation=" + getOperation() + ", ");
        builder.append("priority=" + getPriority() + ", ");
        builder.append("parameter=" + getParameter());
        builder.append("]");
        return builder.toString();
    }
}
