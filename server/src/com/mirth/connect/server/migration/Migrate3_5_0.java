/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.util.ColorUtil;

public class Migrate3_5_0 extends Migrator implements ConfigurationMigrator {
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {
        migrateChannelMetadata();
    }

    private void migrateChannelMetadata() {
        Map<String, Pair<String, Set<String>>> tags = new HashMap<String, Pair<String, Set<String>>>();
        PreparedStatement preparedStatement = null;
        ResultSet results = null;

        try {
            Connection connection = getConnection();

            /*
             * MIRTH-1667: Derby fails if autoCommit is set to true and there are a large number of
             * results. The following error occurs: "ERROR 40XD0: Container has been closed"
             */
            connection.setAutoCommit(false);

            DonkeyElement metadataMapElement = new DonkeyElement("<map/>");

            preparedStatement = connection.prepareStatement("SELECT ID, CHANNEL FROM CHANNEL");
            results = preparedStatement.executeQuery();

            while (results.next()) {
                String channelId = "";

                try {
                    channelId = results.getString(1);
                    String channelXml = results.getString(2);

                    // Add metadata entry for this channel
                    DonkeyElement metadataEntry = metadataMapElement.addChildElement("entry");
                    metadataEntry.addChildElement("string", channelId);
                    DonkeyElement metadataElement = metadataEntry.addChildElement("com.mirth.connect.model.ChannelMetadata");

                    DonkeyElement channelElement = new DonkeyElement(channelXml);

                    // Enabled
                    metadataElement.addChildElement("enabled", channelElement.getChildElement("enabled").getTextContent());

                    // Last modified
                    DonkeyElement lastModifiedElement = channelElement.getChildElement("lastModified");
                    if (lastModifiedElement != null) {
                        try {
                            metadataElement.addChildElementFromXml(lastModifiedElement.toXml());
                        } catch (DonkeyElementException e) {
                        }
                    }

                    DonkeyElement propertiesElement = channelElement.getChildElement("properties");

                    // Pruning settings
                    DonkeyElement pruningSettingsElement = metadataElement.addChildElement("pruningSettings");
                    DonkeyElement pruneMetaDataDaysElement = propertiesElement.getChildElement("pruneMetaDataDays");
                    DonkeyElement pruneContentDaysElement = propertiesElement.getChildElement("pruneContentDays");
                    DonkeyElement archiveEnabledElement = propertiesElement.getChildElement("archiveEnabled");
                    if (pruneMetaDataDaysElement != null) {
                        pruningSettingsElement.addChildElement("pruneMetaDataDays", pruneMetaDataDaysElement.getTextContent());
                    }
                    if (pruneContentDaysElement != null) {
                        pruningSettingsElement.addChildElement("pruneContentDays", pruneContentDaysElement.getTextContent());
                    }
                    if (archiveEnabledElement != null) {
                        pruningSettingsElement.addChildElement("archiveEnabled", archiveEnabledElement.getTextContent());
                    }

                    // Tags
                    DonkeyElement tagsElement = propertiesElement.getChildElement("tags");
                    if (tagsElement != null) {
                        try {
                            Set<String> channelTags = ObjectXMLSerializer.getInstance().deserialize(tagsElement.toString(), Set.class);
                            for (String tagName : channelTags) {
                                tagName = ChannelTag.fixName(tagName);
                                Pair<String, Set<String>> tagInfo = tags.get(tagName.toLowerCase());

                                if (tagInfo == null) {
                                    tagInfo = new ImmutablePair<String, Set<String>>(tagName, new HashSet<String>());
                                    tags.put(tagName.toLowerCase(), tagInfo);
                                }

                                tagInfo.getRight().add(channelId);
                            }
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error migrating metadata for channel " + channelId + ".", e);
                }
            }

            PreparedStatement updateStatement = null;
            try {
                updateStatement = connection.prepareStatement("INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'channelMetadata', ?)");
                updateStatement.setString(1, metadataMapElement.toXml());
                updateStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(updateStatement);
            }

            DonkeyElement tagsElement = new DonkeyElement("<set/>");
            if (MapUtils.isNotEmpty(tags)) {
                for (Pair<String, Set<String>> tag : tags.values()) {
                    DonkeyElement tagElement = tagsElement.addChildElement("channelTag");
                    tagElement.addChildElement("id", UUID.randomUUID().toString());
                    tagElement.addChildElement("name", ChannelTag.fixName(tag.getLeft()));

                    DonkeyElement channelIds = tagElement.addChildElement("channelIds");
                    for (String channelId : tag.getRight()) {
                        channelIds.addChildElement("string", channelId);
                    }

                    Color newColor = ColorUtil.getNewColor();
                    DonkeyElement bgColor = tagElement.addChildElement("backgroundColor");
                    bgColor.addChildElement("red", String.valueOf(newColor.getRed()));
                    bgColor.addChildElement("blue", String.valueOf(newColor.getBlue()));
                    bgColor.addChildElement("green", String.valueOf(newColor.getGreen()));
                    bgColor.addChildElement("alpha", String.valueOf(newColor.getAlpha()));
                }
            }

            updateStatement = null;
            try {
                updateStatement = connection.prepareStatement("INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'channelTags', ?)");
                updateStatement.setString(1, tagsElement.toXml());
                updateStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(updateStatement);
            }

            connection.commit();
        } catch (Exception e) {
            logger.error("Error migrating channel metadata.", e);
        } finally {
            DbUtils.closeQuietly(results);
            DbUtils.closeQuietly(preparedStatement);
        }
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        Map<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();

        propertiesToAdd.put("server.api.accesscontrolalloworigin", new MutablePair<Object, String>("*", "CORS headers"));
        propertiesToAdd.put("server.api.accesscontrolallowcredentials", "false");
        propertiesToAdd.put("server.api.accesscontrolallowmethods", "GET, POST, DELETE, PUT");
        propertiesToAdd.put("server.api.accesscontrolallowheaders", "Content-Type");
        propertiesToAdd.put("server.api.accesscontrolexposeheaders", "");
        propertiesToAdd.put("server.api.accesscontrolmaxage", "");

        propertiesToAdd.put("https.ephemeraldhkeysize", new MutablePair<Object, String>("2048", "Ephemeral Diffie-Hellman key size"));

        return propertiesToAdd;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {
        String[] cipherSuites = configuration.getStringArray("https.ciphersuites");

        if (ArrayUtils.isNotEmpty(cipherSuites)) {
            Set<String> set = new LinkedHashSet<String>();
            for (String cipherSuite : cipherSuites) {
                set.addAll(Arrays.asList(StringUtils.split(cipherSuite, ',')));
            }
            set.removeAll(Arrays.asList(new String[] { "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                    "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                    "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                    "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA" }));

            String oldValue = StringUtils.join(cipherSuites, ",");
            String newValue = StringUtils.join(set, ",");

            // Don't do anything if the old and new values are the same
            if (!StringUtils.equals(oldValue, newValue)) {
                // Only add the old value as a separate property if it's non-default
                if (!StringUtils.equals(oldValue, "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_DSS_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,TLS_EMPTY_RENEGOTIATION_INFO_SCSV")) {
                    configuration.setProperty("https.ciphersuites.old", oldValue);
                    configuration.getLayout().setBlancLinesBefore("https.ciphersuites.old", 1);
                    configuration.getLayout().setComment("https.ciphersuites.old", "In version 3.5.0, Triple DES cipher suites were removed automatically due to known vulnerabilities. The old value for https.ciphersuites, in case you need it, is below.\nIf you no longer need it, you can delete this property.");
                }

                configuration.setProperty("https.ciphersuites", newValue);

                try {
                    configuration.save();
                } catch (ConfigurationException e) {
                    logger.warn("Unable to update HTTPS cipher suites during migration.", e);
                }
            }
        }
    }
}