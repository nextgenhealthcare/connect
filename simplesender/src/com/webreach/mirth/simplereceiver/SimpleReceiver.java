package com.webreach.mirth.simplereceiver;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleReceiver
{
	protected int count = 0;
	protected long startTime;
	protected int lastCount = 0;
	protected long lastTime;

	public static void main(String args[]) throws Exception
	{

		if (args.length != 1)
		{
			System.out.println("Correct usage:  java SimpleReceiver <port>");
			return;
		}
		SimpleReceiver simpleReceiver = new SimpleReceiver();
		simpleReceiver.receive(Integer.parseInt(args[0]));
	}

	public void receive(int port) throws Exception
	{
		startTime = System.currentTimeMillis();
		lastTime = startTime;
		ServerSocket ssock = new ServerSocket(port);
		System.out.println("Server listening on port " + port);

		// display stats periodically
		new Thread()
		{
			public void run()
			{
				try
				{
					while (true)
					{
						Thread.sleep(10000);

						long now = System.currentTimeMillis();

						int diffCount = count - lastCount;
						long diffTime = now - lastTime;
						double msgsPerSec = 1000.0 * diffCount / diffTime;
						long allDiffTime = now - startTime;
						double allMsgsPerSec = 1000.0 * count / allDiffTime;
						System.out.println("==== " + count + " messages received, avg last 10 sec: " + Math.round(msgsPerSec * 100.0) / 100.0
								+ " msgs/sec, avg overall: " + Math.round(allMsgsPerSec * 100.0) / 100.0 + " msgs/sec (" + Thread.activeCount() + " threads).");

						lastCount = count;
						lastTime = now;
					}
				}
				catch (InterruptedException e)
				{
					// should happen when closed.
				}
			}
		}.start();

		while (true)
		{
			final Socket sock = ssock.accept();
			new Thread()
			{
				public void run()
				{
					try
					{
						serve(sock.getInputStream(), new PrintStream(sock.getOutputStream()));
						sock.close();
					}
					catch (Exception e)
					{
						System.out.println("Exception: " + e);
					}
				}
			}.start();
		}
	}

	public void serve(InputStream in, PrintStream out)
	{
		out.println("MSH|junk|junk|junk");
		incrementCount();
	}

	public synchronized void incrementCount()
	{
		count++;
	}
}
