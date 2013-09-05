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
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("encryptionSettings")
public class EncryptionSettings extends AbstractSettings implements Serializable, Auditable {
    private static final long serialVersionUID = 1L;
    
    public static final String ENCRYPTION_PREFIX = "{enc}";
    
    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES";
    public static final String DEFAULT_DIGEST_ALGORITHM = "SHA256";
    public static final String DEFAULT_SECURTITY_PROVIDER = BouncyCastleProvider.class.getName();
    public static final Integer DEFAULT_ENCRYPTION_KEY_LENGTH = 128;

    private static final String ENCRYPTION_EXPORT = "encryption.export";
    private static final String ENCRYPTION_PROPERTIES = "encryption.properties";
    private static final String ENCRYPTION_ALGORITHM = "encryption.algorithm";
    private static final String ENCRYPTION_KEY_LENGTH = "encryption.keylength";
    private static final String DIGEST_ALGORITHM = "digest.algorithm";
    private static final String SECURITY_PROVIDER = "security.provider";

    private Boolean encryptExport;
    private Boolean encryptProperties;
    private String encryptionAlgorithm;
    private Integer encryptionKeyLength;
    private String digestAlgorithm;
    private String securityProvider;
    private byte[] secretKey;

    public EncryptionSettings() {

    }

    public EncryptionSettings(Properties properties) {
        setProperties(properties);
    }

    public Boolean getEncryptExport() {
        return encryptExport;
    }

    public void setEncryptExport(Boolean encryptExport) {
        this.encryptExport = encryptExport;
    }

    public Boolean getEncryptProperties() {
        return encryptProperties;
    }

    public void setEncryptProperties(Boolean encryptProperties) {
        this.encryptProperties = encryptProperties;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public Integer getEncryptionKeyLength() {
        return encryptionKeyLength;
    }

    public void setEncryptionKeyLength(Integer encryptionKeyLength) {
        this.encryptionKeyLength = encryptionKeyLength;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(String securityProvider) {
        this.securityProvider = securityProvider;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public void setProperties(Properties properties) {
        setEncryptExport(intToBooleanObject(properties.getProperty(ENCRYPTION_EXPORT), false));
        setEncryptProperties(intToBooleanObject(properties.getProperty(ENCRYPTION_PROPERTIES), false));
        setEncryptionAlgorithm(properties.getProperty(ENCRYPTION_ALGORITHM, DEFAULT_ENCRYPTION_ALGORITHM));
        setEncryptionKeyLength(toIntegerObject(properties.getProperty(ENCRYPTION_KEY_LENGTH), DEFAULT_ENCRYPTION_KEY_LENGTH));
        setDigestAlgorithm(properties.getProperty(DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM));
        setSecurityProvider(properties.getProperty(SECURITY_PROVIDER, DEFAULT_SECURTITY_PROVIDER));
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        if (getEncryptExport() != null) {
            properties.put(ENCRYPTION_EXPORT, getEncryptExport());
        }

        if (getEncryptProperties() != null) {
            properties.put(ENCRYPTION_PROPERTIES, getEncryptProperties());
        }

        if (getEncryptionAlgorithm() != null) {
            properties.put(ENCRYPTION_ALGORITHM, getEncryptionAlgorithm());
        }

        if (getEncryptionKeyLength() != null) {
            properties.put(ENCRYPTION_KEY_LENGTH, getEncryptionKeyLength().toString());
        }

        if (getDigestAlgorithm() != null) {
            properties.put(DIGEST_ALGORITHM, getDigestAlgorithm());
        }

        if (getSecurityProvider() != null) {
            properties.put(SECURITY_PROVIDER, getSecurityProvider());
        }

        return properties;
    }

    @Override
    public String toAuditString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

}
