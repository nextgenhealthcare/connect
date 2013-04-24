package com.mirth.connect.model.alert;

import org.apache.commons.lang.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertActionProtocol")
public enum AlertActionProtocol {
    EMAIL, CHANNEL;
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString());
    }
}
