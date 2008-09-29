package com.webreach.mirth.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("library")
public class ExtensionLibrary {
    public enum Type {
        SERVER, CLIENT, SHARED
    }

    @XStreamAsAttribute
    private String path;

    @XStreamAsAttribute
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