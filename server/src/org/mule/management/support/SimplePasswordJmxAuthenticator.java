package org.mule.management.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.management.agents.JmxAgent;

public class SimplePasswordJmxAuthenticator implements JMXAuthenticator {
	protected static final Log logger = LogFactory.getLog(JmxAgent.class);

	private Map credentials = new HashMap();

	public Subject authenticate(Object authToken) {
		if (authToken == null) {
			throw new SecurityException("No authentication token available");
		}
		if (!(authToken instanceof String[]) || ((String[]) authToken).length != 2) {
			throw new SecurityException("Unsupported credentials format");
		}

		String[] authentication = (String[]) authToken;

		String username = StringUtils.defaultString(authentication[0]);
		String password = StringUtils.defaultString(authentication[1]);

		if (!credentials.containsKey(username)) {
			throw new SecurityException("Unauthenticated user: " + username);
		}

		if (!password.equals(ObjectUtils.toString(credentials.get(username)))) {
			throw new SecurityException("Invalid password");
		}

		Set principals = new HashSet();
		principals.add(new JMXPrincipal(username));
		return new Subject(true, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);
	}

	public void setCredentials(final Map newCredentials) {
		this.credentials.clear();

		if (newCredentials == null || newCredentials.isEmpty()) {
			logger.warn("Credentials cache has been purged, remote access will no longer be available");
		} else {
			this.credentials.putAll(newCredentials);
		}
	}
}
