package com.webreach.mirth.connectors.sftp;

import org.apache.commons.pool.PoolableObjectFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SftpConnectionFactory implements PoolableObjectFactory {
	private String username;
	private String password;
	private String host;
	private String path;
	int port;
	
	public SftpConnectionFactory(String host, int port, String username, String password, String path) {
		this.username = username;
		this.password = password;
		this.port = port;
		this.host = host;
		this.path = path;
	}
	
	public Object makeObject() throws Exception {
		JSch jsch = new JSch();
		ChannelSftp client = new ChannelSftp();

		try {
			Session session = null;

			if (port > 0) {
				session = jsch.getSession(username, host, port);
			} else {
				session = jsch.getSession(username, host);
			}

			UserInfo userInfo = new SftpUserInfo(password);
			session.setUserInfo(userInfo);
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			client = (ChannelSftp) channel;

			client.cd(path);
		} catch (Exception e) {
			if (client.isConnected()) {
				client.disconnect();
			}

			throw e;
		}

		return client;
	}

	public void destroyObject(Object obj) throws Exception {
		ChannelSftp client = (ChannelSftp) obj;
		client.quit();
		client.disconnect();
	}

	public boolean validateObject(Object obj) {
		ChannelSftp client = (ChannelSftp) obj;
		return client.isConnected();
	}

	public void activateObject(Object obj) throws Exception {

	}

	public void passivateObject(Object obj) throws Exception {

	}
}
