package org.mule.providers;

import com.webreach.mirth.model.QueuedMessage;

public interface QueueEnabledMessageDispatcher {

	public boolean sendPayload(QueuedMessage thePayload) throws Exception;
}
