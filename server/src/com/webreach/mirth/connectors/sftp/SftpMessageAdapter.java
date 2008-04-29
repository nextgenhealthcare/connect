package com.webreach.mirth.connectors.sftp;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

import com.jcraft.jsch.ChannelSftp;

public class SftpMessageAdapter extends AbstractMessageAdapter {
	private byte[] message;

	public SftpMessageAdapter(Object message) throws MessagingException {
		if (message instanceof byte[]) {
			this.message = (byte[]) message;
		} else if (message instanceof ChannelSftp.LsEntry){
			this.message = ((ChannelSftp.LsEntry)message).getFilename().getBytes();
		}else {
			throw new MessageTypeNotSupportedException(message, getClass());
		}
	}

	public String getPayloadAsString() throws Exception {
		return new String(message);
	}

	public byte[] getPayloadAsBytes() throws Exception {
		return message;
	}

	public Object getPayload() {
		return message;
	}

}
