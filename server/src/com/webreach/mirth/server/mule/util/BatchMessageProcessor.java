package com.webreach.mirth.server.mule.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BatchMessageProcessor {
	private byte startOfMessage = (byte) 0x0B;
	private byte endOfMessage = (byte) 0x1C;
	private byte endOfRecord = (byte) 0x0D;

	public BatchMessageProcessor() {

	}

	public BatchMessageProcessor(byte startOfMessage, byte endOfMessage, byte endOfRecord) {
		this.startOfMessage = startOfMessage;
		this.endOfMessage = endOfMessage;
		this.endOfRecord = endOfRecord;
	}

	public List<String> processHL7Messages(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(file);
		return processHL7Messages(scanner);
	}

	public List<String> processHL7Messages(String messages) {
		Scanner scanner = new Scanner(messages);
		return processHL7Messages(scanner);
	}

	private List<String> processHL7Messages(Scanner scanner) {
		ArrayList<String> messages = new ArrayList<String>();
		StringBuilder message = new StringBuilder();

		char data[] = { (char) startOfMessage, (char) endOfMessage };

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().replaceAll(new String(data, 0, 1), "").replaceAll(new String(data, 1, 1), "");

			if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
				if (message.length() > 0) {
					messages.add(message.toString());
					message = new StringBuilder();
				}

				while ((line.length() == 0) && scanner.hasNextLine()) {
					line = scanner.nextLine();
				}

				if (line.length() > 0) {
					message.append(line);
					message.append((char) endOfRecord);
				}
			} else {
				message.append(line);
				message.append((char) endOfRecord);

				if (!scanner.hasNextLine()) {
					messages.add(message.toString());
					message = new StringBuilder();
				}
			}
		}

		scanner.close();
		return messages;
	}

	public byte getEndOfMessage() {
		return endOfMessage;
	}

	public void setEndOfMessage(byte endOfMessage) {
		this.endOfMessage = endOfMessage;
	}

	public byte getEndOfRecord() {
		return endOfRecord;
	}

	public void setEndOfRecord(byte endOfRecord) {
		this.endOfRecord = endOfRecord;
	}

	public byte getStartOfMessage() {
		return startOfMessage;
	}

	public void setStartOfMessage(byte startOfMessage) {
		this.startOfMessage = startOfMessage;
	}
}