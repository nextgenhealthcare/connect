package org.mule.providers.sftp;

import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.resource.spi.work.Work;

import org.apache.log4j.Logger;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SftpMessageReceiver extends PollingMessageReceiver {
	private Logger logger = Logger.getLogger(this.getClass());
	protected SftpConnector connector;
	private FilenameFilter filenameFilter = null;
	protected Set currentFiles = Collections.synchronizedSet(new HashSet());
	protected ChannelSftp sftp;

	public SftpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.connector = (SftpConnector) connector;

		if (endpoint.getFilter() instanceof FilenameFilter) {
			filenameFilter = (FilenameFilter) endpoint.getFilter();
		}
	}

	public void poll() throws Exception {
		List files = listFiles();

		for (Iterator iter = files.iterator(); iter.hasNext();) {
			final ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) iter.next();

			if (!currentFiles.contains(entry.getFilename())) {
				getWorkManager().scheduleWork(new Work() {
					public void run() {
						try {
							currentFiles.add(entry.getFilename());
							processFile(entry);
						} catch (Exception e) {
							connector.handleException(e);
						} finally {
							currentFiles.remove(entry.getFilename());
						}
					}

					public void release() {}
				});
			}

		}
	}

	protected List listFiles() throws Exception {
		UMOEndpointURI uri = endpoint.getEndpointURI();
		Vector entries = sftp.ls(uri.getPath());

		if (entries != null) {
			for (int i = 0; i < entries.size(); i++) {
				Object entry = entries.elementAt(i);

				if (entry instanceof ChannelSftp.LsEntry) {
					System.out.println(((ChannelSftp.LsEntry) entry).getLongname());
				}
			}
		}

		List<ChannelSftp.LsEntry> files = new ArrayList<ChannelSftp.LsEntry>();

		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) iter.next();

			if ((filenameFilter == null) || (filenameFilter.accept(null, entry.getFilename()))) {
				files.add(entry);
			}
		}

		return files;
	}

	protected void processFile(ChannelSftp.LsEntry entry) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		sftp.get(entry.getFilename(), baos);
		UMOMessage message = new MuleMessage(connector.getMessageAdapter(baos.toByteArray()));
		message.setProperty(SftpConnector.PROPERTY_FILENAME, entry.getFilename());
		routeMessage(message);
		sftp.rm(entry.getFilename());
	}

	public void doConnect() throws Exception {
		UMOEndpointURI uri = endpoint.getEndpointURI();
		JSch jsch = new JSch();
		System.out.println("connecting to server: " + uri.getUsername() + ":" + uri.getPassword() + "@" + uri.getHost() + ":" + uri.getPort());
		Session session = jsch.getSession(uri.getUsername(), uri.getHost(), uri.getPort());
		UserInfo userInfo = new SftpUserInfo(uri.getUsername(), uri.getPassword());
		session.setUserInfo(userInfo);
		session.connect();
		Channel channel = session.openChannel("sftp");
		channel.connect();
		sftp = (ChannelSftp) channel;
	}

	public void doDisconnect() throws Exception {
		if (sftp != null) {
			sftp.disconnect();
		}
	}
}
