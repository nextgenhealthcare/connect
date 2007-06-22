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
