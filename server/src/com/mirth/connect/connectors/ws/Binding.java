package com.mirth.connect.connectors.ws;

import javax.xml.ws.soap.SOAPBinding;

public enum Binding {
    DEFAULT("Default", null), SOAP11HTTP("SOAP 1.1", SOAPBinding.SOAP11HTTP_BINDING), SOAP12HTTP(
            "SOAP 1.2", "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/");

    private String name;
    private String value;

    private Binding(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static Binding fromDisplayName(String displayName) {
        for (Binding binding : Binding.values()) {
            if (binding.getName().equals(displayName)) {
                return binding;
            }
        }

        return null;
    }
}