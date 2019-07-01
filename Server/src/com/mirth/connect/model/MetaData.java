/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.client.core.Version;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public abstract class MetaData {
    @XStreamAsAttribute
    private String path;

    private String name;
    private String author;
    private String mirthVersion;
    private String pluginVersion;
    private String url;
    private String description;
    @XStreamAlias("apiProviders")
    @XStreamImplicit(itemFieldName = "apiProvider")
    private List<ApiProvider> apiProviders;
    @XStreamAlias("libraries")
    @XStreamImplicit(itemFieldName = "library")
    private List<ExtensionLibrary> libraries;
    private String templateClassName;
    private List<String> userutilPackages;
    private Boolean notify;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMirthVersion() {
        return mirthVersion;
    }

    public void setMirthVersion(String mirthVersion) {
        this.mirthVersion = mirthVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ApiProvider> getApiProviders() {
        return apiProviders;
    }

    public List<ApiProvider> getApiProviders(Version version) {
        List<ApiProvider> list = new ArrayList<ApiProvider>();

        if (CollectionUtils.isNotEmpty(apiProviders)) {
            for (ApiProvider provider : apiProviders) {
                boolean valid = true;
                Version minVersion = Version.fromString(provider.getMinVersion());
                Version maxVersion = Version.fromString(provider.getMaxVersion());

                if (minVersion != null && minVersion.ordinal() > version.ordinal()) {
                    valid = false;
                }
                if (maxVersion != null && maxVersion.ordinal() < version.ordinal()) {
                    valid = false;
                }

                if (valid) {
                    list.add(provider);
                }
            }
        }

        return list;
    }

    public void setApiProviders(List<ApiProvider> apiProviders) {
        this.apiProviders = apiProviders;
    }

    public List<ExtensionLibrary> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<ExtensionLibrary> libraries) {
        this.libraries = libraries;
    }

    public String getTemplateClassName() {
        return templateClassName;
    }

    public void setTemplateClassName(String templateClassName) {
        this.templateClassName = templateClassName;
    }

    public List<String> getUserutilPackages() {
        return userutilPackages;
    }

    public void setUserutilPackages(List<String> userutilPackages) {
        this.userutilPackages = userutilPackages;
    }

    public Boolean getNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
