/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("pluginMetaData")
public class PluginMetaData extends MetaData implements Serializable {
    @XStreamAlias("serverClasses")
    private List<PluginClass> serverClasses;

    @XStreamAlias("clientClasses")
    private List<PluginClass> clientClasses;

    private String migratorClass;
    private String sqlScript;

    @XStreamAlias("sqlMapConfigs")
    private Map<String, String> sqlMapConfigs;

    public List<PluginClass> getServerClasses() {
        return serverClasses;
    }

    public void setServerClasses(List<PluginClass> serverClasses) {
        this.serverClasses = serverClasses;
    }

    public List<PluginClass> getClientClasses() {
        return clientClasses;
    }

    public void setClientClasses(List<PluginClass> clientClasses) {
        this.clientClasses = clientClasses;
    }

    public String getMigratorClass() {
        return migratorClass;
    }

    public void setMigratorClass(String migratorClass) {
        this.migratorClass = migratorClass;
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

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class MigratorClassInfo {
        @XStreamAsAttribute
        private String version;
        @XStreamAsAttribute
        private String name;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getClassName() {
            return name;
        }

        public void setClassName(String className) {
            this.name = className;
        }
    }
}
