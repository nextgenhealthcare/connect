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
public class Command implements Comparable {
	public static final int CMD_SHUTDOWN = 9;
	public static final int CMD_START_MULE = 101;
	public static final int CMD_STOP_MULE = 102;
	public static final int CMD_RESTART_MULE = 103;

	public static final int PRIORITY_NORMAL = 0;
	public static final int PRIORITY_HIGH = 1;

	private int command;
	private Object parameter;
	private int priority;

	public Command(int command) {
		this(command, null, PRIORITY_NORMAL);
	}

	public Command(int command, Object parameter) {
		this(command, parameter, PRIORITY_NORMAL);
	}

	public Command(int command, int parameter) {
		this(command, null, parameter);
	}

	public Command(int command, Object parameter, int priority) {
		this.command = command;
		this.parameter = parameter;
		this.priority = priority;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public Object getParameter() {
		return parameter;
	}

	public void setParameter(Object parameter) {
		this.parameter = parameter;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int compareTo(Object o) {
		Command compareCommand = (Command) o;

		if (getPriority() < compareCommand.getPriority())
			return -1;
		else if (getPriority() > compareCommand.getPriority())
			return 1;
		else
			return 0;
	}

	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(this.getClass().getSimpleName() + "[");
		buffer.append(getCommand() + "/");
		buffer.append(getPriority() + "/");
		buffer.append(getParameter().toString());
		buffer.append("]");
		return buffer.toString();
	}
}
