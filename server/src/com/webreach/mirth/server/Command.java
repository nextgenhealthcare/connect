/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server;

/**
 * A command for the Mirth service. Consists of a command, a parameter, and a
 * priority. Default priority of a command is normal.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class Command implements Comparable<Command> {
	public enum Operation {
		START_SERVER, SHUTDOWN_SERVER, START_ENGINE, STOP_ENGINE, RESTART_ENGINE
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
