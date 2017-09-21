/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.net.URI;

public class FileSystemConnectionOptions {
    private URI uri;
    private boolean anonymous;
    private String username;
    private String password;
    private SchemeProperties schemeProperties;

    public FileSystemConnectionOptions(boolean anonymous, String username, String password, SchemeProperties schemeProperties) {
        this(null, anonymous, username, password, schemeProperties);
    }

    public FileSystemConnectionOptions(URI uri, boolean anonymous, String username, String password, SchemeProperties schemeProperties) {
        this.uri = uri;
        this.anonymous = anonymous;
        this.username = username;
        this.password = password;
        this.schemeProperties = schemeProperties;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SchemeProperties getSchemeProperties() {
        return schemeProperties;
    }

    public void setSchemeProperties(SchemeProperties schemeProperties) {
        this.schemeProperties = schemeProperties;
    }
}
