/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model;

import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.webreach.mirth.util.EqualsUtil;

@XStreamAlias("pluginMetaData")
public class PluginMetaData extends MetaData implements Serializable {
    @XStreamAlias("extensionsPoints")
    @XStreamImplicit(itemFieldName="extensionPoint")
	private List<ExtensionPoint> extensionPoints;

    private String sqlScript;

    @XStreamAlias("sqlMapConfigs")
    private Map<String, String> sqlMapConfigs;

	public List<ExtensionPoint> getExtensionPoints() {
		return extensionPoints;
	}

	public void setExtensionPoints(List<ExtensionPoint> plugins) {
		this.extensionPoints = plugins;
	}

    public String getSqlScript() {
        return sqlScript;
    }

    public void setSqlScript(String sqlScript) {
        this.sqlScript = sqlScript;
    }

    public Map<String, String> getSqlMapConfigs() {
        return sqlMapConfigs;
    }

    public void setSqlMapConfigs(Map<String, String> sqlMapConfigs) {
        this.sqlMapConfigs = sqlMapConfigs;
    }

    public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof PluginMetaData)) {
			return false;
		}

		PluginMetaData plugin = (PluginMetaData) that;

		return
			EqualsUtil.areEqual(this.getName(), plugin.getName()) &&
			EqualsUtil.areEqual(this.getAuthor(), plugin.getAuthor()) &&
			EqualsUtil.areEqual(this.getPluginVersion(), plugin.getPluginVersion()) &&
			EqualsUtil.areEqual(this.getMirthVersion(), plugin.getMirthVersion()) &&
			EqualsUtil.areEqual(this.isEnabled(), plugin.isEnabled()) &&
			EqualsUtil.areEqual(this.getExtensionPoints(), plugin.getExtensionPoints()) &&
			EqualsUtil.areEqual(this.getDescription(), plugin.getDescription());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		builder.append("author=" + getAuthor().toString() + ", ");
		builder.append("pluginVersion=" + getPluginVersion() + ", ");
		builder.append("mirthVersion=" + getMirthVersion() + ", ");
		
		for (ListIterator<ExtensionPoint> iter = extensionPoints.listIterator(); iter.hasNext();) {
			builder.append("extention-point" + iter.nextIndex() + "=" + iter.next().toString() + ", ");
		}
		
		builder.append("description=" + getDescription() + ", ");
		builder.append("enabled=" + isEnabled() + "]");
		return builder.toString();
	}
}
