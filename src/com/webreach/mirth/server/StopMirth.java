package com.webreach.mirth.server;

public class StopMirth {
	public static void main(String[] args) {
		CommandQueue.getInstance().addCommand(new Command(Command.Operation.STOP));
	}

}
