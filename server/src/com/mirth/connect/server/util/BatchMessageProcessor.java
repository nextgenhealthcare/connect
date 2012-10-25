/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class BatchMessageProcessor {
	private byte startOfMessage = (byte) 0x0B;
	private byte endOfMessage = (byte) 0x1C;
	private byte endOfRecord = (byte) 0x0D;
    private Pattern delimiterPattern = Pattern.compile("\r\n|\r|\n");
    
	public BatchMessageProcessor() {

	}

	public BatchMessageProcessor(byte startOfMessage, byte endOfMessage, byte endOfRecord) {
		this.startOfMessage = startOfMessage;
		this.endOfMessage = endOfMessage;
		this.endOfRecord = endOfRecord;
	}

	// ast: change the File to a java.io.InputStreamReader
	public List<String> processHL7Messages(InputStreamReader is) {
		Scanner scanner = new Scanner(is);
		return processHL7Messages(scanner);
	}

	public List<String> processHL7Messages(String messages) {
		Scanner scanner = new Scanner(messages);
		return processHL7Messages(scanner);
	}

	private List<String> processHL7Messages(Scanner scanner) {
		scanner.useDelimiter(delimiterPattern);
		ArrayList<String> messages = new ArrayList<String>();
		StringBuilder message = new StringBuilder();
		char data[] = { (char) startOfMessage, (char) endOfMessage };
		while (scanner.hasNext()) {
			String line = scanner.next().replaceAll(new String(data, 0, 1), "").replaceAll(new String(data, 1, 1), "").trim();

			if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
				if (message.length() > 0) {
					messages.add(message.toString());
					message = new StringBuilder();
				}

				while ((line.length() == 0) && scanner.hasNext()) {
					line = scanner.next();
				}

				if (line.length() > 0) {
					message.append(line);
					message.append((char) endOfRecord);
				}
			} else if (line.startsWith("FHS") || line.startsWith("BHS") || line.startsWith("BTS") || line.startsWith("FTS")){
				//ignore batch headers
				if (!scanner.hasNext()) {
					messages.add(message.toString());
					message = new StringBuilder();
				}
			} else {
				message.append(line);
				message.append((char) endOfRecord);

				if (!scanner.hasNext()) {
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
    
    public Pattern getDelimiterPattern() {
        return delimiterPattern;
    }

    public void setDelimiterPattern(Pattern delimiterPattern) {
        this.delimiterPattern = delimiterPattern;
    }    
}
