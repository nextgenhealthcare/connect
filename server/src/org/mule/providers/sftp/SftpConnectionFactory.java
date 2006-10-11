package org.mule.providers.sftp;

import org.apache.commons.pool.PoolableObjectFactory;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SftpConnectionFactory implements PoolableObjectFactory {
	private UMOEndpointURI uri;
	private String username;
	private String password;
	
	public SftpConnectionFactory(UMOEndpointURI uri, String username, String password) {
		this.uri = uri;
		this.username = username;
		this.password = password;
	}

	public Object makeObject() throws Exception {
		JSch jsch = new JSch();
		ChannelSftp client = new ChannelSftp();

		try {
			Session session = null;

			if (uri.getPort() > 0) {
				session = jsch.getSession(username, uri.getHost(), uri.getPort());
			} else {
				session = jsch.getSession(username, uri.getHost());
			}

			UserInfo userInfo = new SftpUserInfo(password);
			session.setUserInfo(userInfo);
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			client = (ChannelSftp) channel;

			// FIXME: find elegant way to work even with starting slash
			String path = uri.getPath().substring(1, uri.getPath().length());
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
