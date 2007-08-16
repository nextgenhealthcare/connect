package com.webreach.mirth.server.mbeans;

import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.Mirth;

public class MirthService implements MirthServiceMBean {
	private Mirth mirthServer = null;
	public void start()	{
		if (mirthServer == null) {
			mirthServer = new Mirth();
			mirthServer.start();
		} else if (!mirthServer.isAlive()) {
			mirthServer = new Mirth();
			mirthServer.start();
		}
	}

	public void stop() {
		CommandQueue queue = CommandQueue.getInstance();
		queue.addCommand(new Command(Command.Operation.SHUTDOWN));
	}
}
