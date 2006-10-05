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

	public SftpConnectionFactory(UMOEndpointURI uri) {
		this.uri = uri;
	}

	public Object makeObject() throws Exception {
		JSch jsch = new JSch();
		ChannelSftp client = new ChannelSftp();

		try {
			Session session = null;

			if (uri.getPort() > 0) {
				session = jsch.getSession(uri.getUsername(), uri.getHost(), uri.getPort());
			} else {
				session = jsch.getSession(uri.getUsername(), uri.getHost());
			}

			UserInfo userInfo = new SftpUserInfo(uri.getPassword());
			session.setUserInfo(userInfo);
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			client = (ChannelSftp) channel;

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
