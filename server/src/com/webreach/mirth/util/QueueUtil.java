package com.webreach.mirth.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.webreach.mirth.server.controllers.ConfigurationController;

public class QueueUtil {
	public static void removeChannelQueuestore(String channelId) throws Exception {
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
			throw new Exception("Could remove queue messages for channel: " + channelId);
		}
	}
}