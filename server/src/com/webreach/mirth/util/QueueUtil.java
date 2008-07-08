package com.webreach.mirth.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class QueueUtil {
	private Log logger = LogFactory.getLog(getClass());
	
	private static QueueUtil instance = null;

	public QueueUtil() {
		
	}
	
	public static QueueUtil getInstance() {
		synchronized (QueueUtil.class) {
			if (instance == null) {
				instance = new QueueUtil();
			}
			
			return instance;
		}
	}
	
	public void removeChannelQueuestore(String channelId) throws Exception {
		try {
			File queuestoreDir = new File(ConfigurationController.getInstance().getQueuestorePath());

			if (queuestoreDir.exists()) {
				// NOTE: could not use FileUtils here because the listFiles method
				// does not return directories
				String[] files = queuestoreDir.list(new WildcardFileFilter(channelId + "*"));

				for (int i = 0; i < files.length; i++) {
					File file = new File(queuestoreDir.getAbsolutePath() + File.separator + files[i]);
					FileUtils.forceDelete(file);
				}
			}
		} catch (Exception e) {
			logger.error("Could remove queue messages for channel: " + channelId + " \n" + e);
		}
	}
}