/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A Channel is the main element of the Mirth architecture. Channels connect a
 * single source with multiple destinations which are represented by Connectors.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class Channel {
	public enum Direction {
		INBOUND, OUTBOUND
	};

	public enum Mode {
		ROUTER, BROADCAST, APPLICATION
	};

	private int id;
	private String name;
	private String description;
	private boolean enabled;
	private boolean modified;
	private Direction direction;
	private Mode mode;
	private Connector sourceConnector;
	private List<Connector> destinationConnectors = new ArrayList<Connector>();;
	private Properties properties = new Properties();

	public Channel() {}

	public Channel(Direction direction) {
		this.direction = direction;
	}

	public Channel(Direction direction, Mode mode) {
		this.direction = direction;
		this.mode = mode;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isModified() {
		return this.modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Connector getSourceConnector() {
		return this.sourceConnector;
	}

	public void setSourceConnector(Connector sourceConnector) {
		this.sourceConnector = sourceConnector;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public List<Connector> getDestinationConnectors() {
		return this.destinationConnectors;
	}

	public void setDestinationConnectors(List<Connector> destinationConnectors) {
		this.destinationConnectors = destinationConnectors;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
