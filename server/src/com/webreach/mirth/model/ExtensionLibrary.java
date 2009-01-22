package com.webreach.mirth.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ExtensionLibrary implements Serializable {
    public enum Type {
        SERVER, CLIENT, SHARED
    }

    @XStreamAsAttribute
    private String path;

    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}