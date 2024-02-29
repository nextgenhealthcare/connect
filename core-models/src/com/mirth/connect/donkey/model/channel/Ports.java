/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 * 
 * 
 * Used in Query and API call: getPortsInUse
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@SuppressWarnings("serial")
@XStreamAlias("ports")
public class Ports implements Serializable {
    private String name;
    private String id;
    private String port;

    public Ports() {

    }

    public Ports(String id, String name, String port) {
        this.name = name==null ? "" : name.toUpperCase();
        this.id = id;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name==null ? "" : name.toUpperCase();
    }
    
    public void setId(String _id) {
        this.id = _id;
    }

    public String getId() {
        return id;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Ports)) {
            return false;
        }

        Ports newPortsObject = (Ports) object;
        if (newPortsObject.getName() != null && name != null) {
            return (newPortsObject.getName().equals(name) && newPortsObject.getId().equals(id) && newPortsObject.getPort().equals(port));
        } else if (newPortsObject.getName() == null && name == null) {
            return (newPortsObject.getName().equals(name) && newPortsObject.getId().equals(id));
        } else {
            return false;
        }
    }
}
