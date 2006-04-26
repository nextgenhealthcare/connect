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


package com.webreach.mirth;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class MirthCommandQueue {
	private PriorityBlockingQueue<MirthCommand> commandQueue = new PriorityBlockingQueue<MirthCommand>();

	// singleton pattern
	private static MirthCommandQueue instance = null;

	private MirthCommandQueue() {}

	public static MirthCommandQueue getInstance() {
		synchronized (MirthCommandQueue.class) {
			if (instance == null)
				instance = new MirthCommandQueue();

			return instance;
		}
	}

	/**
	 * Adds a <code>MirthCommand</code> to the command queue.
	 * 
	 * @param command
	 */
	public void addCommand(MirthCommand command) {
		commandQueue.put(command);
	}

	/**
	 * Returns the command with the highest priority from the queue.
	 * 
	 * @return the command with the highest priority from the queue.
	 */
	public MirthCommand getCommand() {
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
		for (Iterator<MirthCommand> iter = commandQueue.iterator(); iter.hasNext();) {
			System.out.println(iter.next());
		}
	}
}
