package com.mirth.connect.connectors.file;

import java.io.Serializable;

import com.mirth.connect.donkey.util.purge.Purgable;

public abstract class SchemeProperties implements Serializable, Purgable {
    public SchemeProperties() {}

    public abstract SchemeProperties getFileSchemeProperties();

    public abstract String toFormattedString();

    @Override
    public abstract SchemeProperties clone();
}