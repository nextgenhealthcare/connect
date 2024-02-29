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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.mirth.connect.donkey.util.Serializer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("encryptionSettings")
public class EncryptionSettings extends AbstractSettings implements Serializable, Auditable {
    private static final long serialVersionUID = 1L;

    public static final String ENCRYPTION_PREFIX = "{enc}";

    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String DEFAULT_ENCRYPTION_CHARSET = "UTF-8";
    public static final Integer DEFAULT_ENCRYPTION_KEY_LENGTH = 128;

    public static final String DEFAULT_DIGEST_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final Integer DEFAULT_DIGEST_SALT_SIZE = 8;
    public static final Integer DEFAULT_DIGEST_ITERATIONS = 600000;
    public static final Boolean DEFAULT_DIGEST_USE_PBE = true;
    public static final Integer DEFAULT_DIGEST_KEY_SIZE = 256;

    public static final String DEFAULT_SECURITY_PROVIDER = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    private static final String ENCRYPTION_EXPORT = "encryption.export";
    private static final String ENCRYPTION_PROPERTIES = "encryption.properties";
    private static final String ENCRYPTION_ALGORITHM = "encryption.algorithm";
    private static final String ENCRYPTION_CHARSET = "encryption.charset";
    private static final String ENCRYPTION_FALLBACK_ALGORITHM = "encryption.fallback.algorithm";
    private static final String ENCRYPTION_FALLBACK_CHARSET = "encryption.fallback.charset";
    private static final String ENCRYPTION_KEY_LENGTH = "encryption.keylength";

    private static final String DIGEST_ALGORITHM = "digest.algorithm";
    private static final String DIGEST_SALT_SIZE = "digest.saltsizeinbytes";
    private static final String DIGEST_ITERATIONS = "digest.iterations";
    private static final String DIGEST_USE_PBE = "digest.usepbe";
    private static final String DIGEST_KEY_SIZE = "digest.keysizeinbits";

    private static final String DIGEST_FALLBACK_ALGORITHM = "digest.fallback.algorithm";
    private static final String DIGEST_FALLBACK_SALT_SIZE = "digest.fallback.saltsizeinbytes";
    private static final String DIGEST_FALLBACK_ITERATIONS = "digest.fallback.iterations";
    private static final String DIGEST_FALLBACK_USE_PBE = "digest.fallback.usepbe";
    private static final String DIGEST_FALLBACK_KEY_SIZE = "digest.fallback.keysizeinbits";

    private static final String SECURITY_PROVIDER = "security.provider";

    private Boolean encryptExport;
    private Boolean encryptProperties;
    private String encryptionAlgorithm;
    private String encryptionCharset;
    private String encryptionFallbackAlgorithm;
    private String encryptionFallbackCharset;
    private Integer encryptionKeyLength;
    private String digestAlgorithm;
    private Integer digestSaltSize;
    private Integer digestIterations;
    private Boolean digestUsePBE;
    private Integer digestKeySize;
    private String digestFallbackAlgorithm;
    private Integer digestFallbackSaltSize;
    private Integer digestFallbackIterations;
    private Boolean digestFallbackUsePBE;
    private Integer digestFallbackKeySize;
    private String securityProvider;
    private byte[] secretKey;

    public EncryptionSettings() {

    }

    public EncryptionSettings(Properties properties, Serializer serializer) {
        setProperties(properties, serializer);
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

    public String getEncryptionBaseAlgorithm() {
        if (StringUtils.isNotBlank(encryptionAlgorithm)) {
            int index = StringUtils.indexOf(encryptionAlgorithm, '/');
            if (index >= 0) {
                return encryptionAlgorithm.substring(0, index);
            }
        }
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getEncryptionCharset() {
        return encryptionCharset;
    }

    public void setEncryptionCharset(String encryptionCharset) {
        this.encryptionCharset = encryptionCharset;
    }

    public String getEncryptionFallbackAlgorithm() {
        return encryptionFallbackAlgorithm;
    }

    public void setEncryptionFallbackAlgorithm(String encryptionFallbackAlgorithm) {
        this.encryptionFallbackAlgorithm = encryptionFallbackAlgorithm;
    }

    public String getEncryptionFallbackCharset() {
        return encryptionFallbackCharset;
    }

    public void setEncryptionFallbackCharset(String encryptionFallbackCharset) {
        this.encryptionFallbackCharset = encryptionFallbackCharset;
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

    public Integer getDigestSaltSize() {
        return digestSaltSize;
    }

    public void setDigestSaltSize(Integer digestSaltSize) {
        this.digestSaltSize = digestSaltSize;
    }

    public Integer getDigestIterations() {
        return digestIterations;
    }

    public void setDigestIterations(Integer digestIterations) {
        this.digestIterations = digestIterations;
    }

    public Boolean getDigestUsePBE() {
        return digestUsePBE;
    }

    public void setDigestUsePBE(Boolean digestUsePBE) {
        this.digestUsePBE = digestUsePBE;
    }

    public Integer getDigestKeySize() {
        return digestKeySize;
    }

    public void setDigestKeySize(Integer digestKeySize) {
        this.digestKeySize = digestKeySize;
    }

    public String getDigestFallbackAlgorithm() {
        return digestFallbackAlgorithm;
    }

    public void setDigestFallbackAlgorithm(String digestFallbackAlgorithm) {
        this.digestFallbackAlgorithm = digestFallbackAlgorithm;
    }

    public Integer getDigestFallbackSaltSize() {
        return digestFallbackSaltSize;
    }

    public void setDigestFallbackSaltSize(Integer digestFallbackSaltSize) {
        this.digestFallbackSaltSize = digestFallbackSaltSize;
    }

    public Integer getDigestFallbackIterations() {
        return digestFallbackIterations;
    }

    public void setDigestFallbackIterations(Integer digestFallbackIterations) {
        this.digestFallbackIterations = digestFallbackIterations;
    }

    public Boolean getDigestFallbackUsePBE() {
        return digestFallbackUsePBE;
    }

    public void setDigestFallbackUsePBE(Boolean digestFallbackUsePBE) {
        this.digestFallbackUsePBE = digestFallbackUsePBE;
    }

    public Integer getDigestFallbackKeySize() {
        return digestFallbackKeySize;
    }

    public void setDigestFallbackKeySize(Integer digestFallbackKeySize) {
        this.digestFallbackKeySize = digestFallbackKeySize;
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
    public void setProperties(Properties properties, Serializer serializer) {
        setEncryptExport(intToBooleanObject(properties.getProperty(ENCRYPTION_EXPORT), false));
        setEncryptProperties(intToBooleanObject(properties.getProperty(ENCRYPTION_PROPERTIES), false));
        setEncryptionAlgorithm(properties.getProperty(ENCRYPTION_ALGORITHM, DEFAULT_ENCRYPTION_ALGORITHM));
        setEncryptionCharset(properties.getProperty(ENCRYPTION_CHARSET, DEFAULT_ENCRYPTION_CHARSET));
        setEncryptionFallbackAlgorithm(properties.getProperty(ENCRYPTION_FALLBACK_ALGORITHM, "AES"));
        setEncryptionFallbackCharset(properties.getProperty(ENCRYPTION_FALLBACK_CHARSET, "UTF-8"));
        setEncryptionKeyLength(toIntegerObject(properties.getProperty(ENCRYPTION_KEY_LENGTH), DEFAULT_ENCRYPTION_KEY_LENGTH));
        setDigestAlgorithm(properties.getProperty(DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM));
        setDigestSaltSize(toIntegerObject(properties.getProperty(DIGEST_SALT_SIZE), DEFAULT_DIGEST_SALT_SIZE));
        setDigestIterations(toIntegerObject(properties.getProperty(DIGEST_ITERATIONS), DEFAULT_DIGEST_ITERATIONS));
        setDigestUsePBE(intToBooleanObject(properties.getProperty(DIGEST_USE_PBE), DEFAULT_DIGEST_USE_PBE));
        setDigestKeySize(toIntegerObject(properties.getProperty(DIGEST_KEY_SIZE), DEFAULT_DIGEST_KEY_SIZE));
        setDigestFallbackAlgorithm(properties.getProperty(DIGEST_FALLBACK_ALGORITHM, "SHA256"));
        setDigestFallbackSaltSize(toIntegerObject(properties.getProperty(DIGEST_FALLBACK_SALT_SIZE), 8));
        setDigestFallbackIterations(toIntegerObject(properties.getProperty(DIGEST_FALLBACK_ITERATIONS), 1000));
        setDigestFallbackUsePBE(intToBooleanObject(properties.getProperty(DIGEST_FALLBACK_USE_PBE), false));
        setDigestFallbackKeySize(toIntegerObject(properties.getProperty(DIGEST_FALLBACK_KEY_SIZE), 256));
        setSecurityProvider(properties.getProperty(SECURITY_PROVIDER, DEFAULT_SECURITY_PROVIDER));
    }

    @Override
    public Properties getProperties(Serializer serializer) {
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

        if (getEncryptionCharset() != null) {
            properties.put(ENCRYPTION_CHARSET, getEncryptionCharset());
        }

        if (getEncryptionFallbackAlgorithm() != null) {
            properties.put(ENCRYPTION_FALLBACK_ALGORITHM, getEncryptionFallbackAlgorithm());
        }

        if (getEncryptionFallbackCharset() != null) {
            properties.put(ENCRYPTION_FALLBACK_CHARSET, getEncryptionFallbackCharset());
        }

        if (getEncryptionKeyLength() != null) {
            properties.put(ENCRYPTION_KEY_LENGTH, getEncryptionKeyLength().toString());
        }

        if (getDigestAlgorithm() != null) {
            properties.put(DIGEST_ALGORITHM, getDigestAlgorithm());
        }

        if (getDigestSaltSize() != null) {
            properties.put(DIGEST_SALT_SIZE, getDigestSaltSize());
        }

        if (getDigestIterations() != null) {
            properties.put(DIGEST_ITERATIONS, getDigestIterations());
        }

        if (getDigestUsePBE() != null) {
            properties.put(DIGEST_USE_PBE, getDigestUsePBE());
        }

        if (getDigestKeySize() != null) {
            properties.put(DIGEST_KEY_SIZE, getDigestKeySize());
        }

        if (getDigestFallbackAlgorithm() != null) {
            properties.put(DIGEST_FALLBACK_ALGORITHM, getDigestFallbackAlgorithm());
        }

        if (getDigestFallbackSaltSize() != null) {
            properties.put(DIGEST_FALLBACK_SALT_SIZE, getDigestFallbackSaltSize());
        }

        if (getDigestFallbackIterations() != null) {
            properties.put(DIGEST_FALLBACK_ITERATIONS, getDigestFallbackIterations());
        }

        if (getDigestFallbackUsePBE() != null) {
            properties.put(DIGEST_FALLBACK_USE_PBE, getDigestFallbackUsePBE());
        }

        if (getDigestFallbackKeySize() != null) {
            properties.put(DIGEST_FALLBACK_KEY_SIZE, getDigestFallbackKeySize());
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
