/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mirth.connect.donkey.model.DatabaseConstants;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.DonkeyStatisticsUpdater;
import com.mirth.connect.donkey.server.data.jdbc.DBCPConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.HikariConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.XmlQuerySource;
import com.mirth.connect.donkey.server.data.jdbc.XmlQuerySource.XmlQuerySourceException;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.SerializerProvider;
import com.mirth.connect.donkey.util.xstream.XStreamSerializer;

public class Donkey {
    @Inject
    private static Donkey instance;

    public static Donkey getInstance() {
        synchronized (Donkey.class) {
            if (instance == null) {
                instance = new Donkey();
            }

            return instance;
        }
    }

    private Donkey() {

    }

    private Map<String, Channel> deployedChannels = new ConcurrentHashMap<String, Channel>();
    private DonkeyConfiguration donkeyConfiguration;
    private DonkeyDaoFactory daoFactory;
    private DonkeyDaoFactory readOnlyDaoFactory;
    private Serializer serializer = new XStreamSerializer();
    private Encryptor encryptor;
    private EventDispatcher eventDispatcher;
    private DonkeyStatisticsUpdater statisticsUpdater;
    private Logger logger = Logger.getLogger(getClass());
    private boolean running = false;

    public void initEngine(DonkeyConfiguration donkeyConfiguration) throws StartException {
        this.donkeyConfiguration = donkeyConfiguration;

        Properties dbProperties = donkeyConfiguration.getDonkeyProperties();
        String database = dbProperties.getProperty(DatabaseConstants.DATABASE);
        String driver = dbProperties.getProperty(DatabaseConstants.DATABASE_DRIVER);
        String url = dbProperties.getProperty(DatabaseConstants.DATABASE_URL);
        String username = dbProperties.getProperty(DatabaseConstants.DATABASE_USERNAME);
        String password = dbProperties.getProperty(DatabaseConstants.DATABASE_PASSWORD);
        String pool = dbProperties.getProperty(DatabaseConstants.DATABASE_POOL);
        boolean jdbc4 = Boolean.parseBoolean(dbProperties.getProperty(DatabaseConstants.DATABASE_JDBC4));
        String testQuery = dbProperties.getProperty(DatabaseConstants.DATABASE_TEST_QUERY);
        int maxConnections;

        try {
            maxConnections = Integer.parseInt(dbProperties.getProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS));
        } catch (NumberFormatException e) {
            throw new StartException("Failed to read the " + DatabaseConstants.DATABASE_MAX_CONNECTIONS + " configuration property");
        }

        SerializerProvider serializerProvider = new SerializerProvider() {
            @Override
            public Serializer getSerializer(Integer metaDataId) {
                return serializer;
            }
        };

        XmlQuerySource xmlQuerySource = new XmlQuerySource();

        try {
            xmlQuerySource.load("default.xml");
            xmlQuerySource.load(dbProperties.getProperty("database") + ".xml");
        } catch (XmlQuerySourceException e) {
            throw new StartException(e);
        }

        daoFactory = createDaoFactory(database, driver, url, username, password, pool, jdbc4, testQuery, maxConnections, serializerProvider, xmlQuerySource, false);

        boolean splitReadWrite = Boolean.parseBoolean(dbProperties.getProperty(DatabaseConstants.DATABASE_ENABLE_READ_WRITE_SPLIT));

        if (splitReadWrite) {
            String readOnlyDatabase = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY, database);
            String readOnlyDriver = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_DRIVER, driver);
            String readOnlyUrl = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_URL, url);
            String readOnlyUsername = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_USERNAME, username);
            String readOnlyPassword = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_PASSWORD, password);
            String readOnlyPool = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_POOL, pool);
            boolean readOnlyJdbc4 = Boolean.parseBoolean(dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_JDBC4, dbProperties.getProperty(DatabaseConstants.DATABASE_JDBC4)));
            String readOnlyTestQuery = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_TEST_QUERY, testQuery);
            int readOnlyMaxConnections;

            try {
                readOnlyMaxConnections = Integer.parseInt(dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_MAX_CONNECTIONS, dbProperties.getProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS)));
            } catch (NumberFormatException e) {
                throw new StartException("Failed to read the " + DatabaseConstants.DATABASE_READONLY_MAX_CONNECTIONS + " configuration property");
            }

            readOnlyDaoFactory = createDaoFactory(readOnlyDatabase, readOnlyDriver, readOnlyUrl, readOnlyUsername, readOnlyPassword, readOnlyPool, readOnlyJdbc4, readOnlyTestQuery, readOnlyMaxConnections, serializerProvider, xmlQuerySource, true);
        } else {
            readOnlyDaoFactory = daoFactory;
        }
    }

    private JdbcDaoFactory createDaoFactory(String database, String driver, String url, String username, String password, String pool, boolean jdbc4, String testQuery, int maxConnections, SerializerProvider serializerProvider, XmlQuerySource xmlQuerySource, boolean readOnly) throws StartException {
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new StartException(e);
            }
        }

        JdbcDaoFactory jdbcDaoFactory = JdbcDaoFactory.getInstance(database);
        jdbcDaoFactory.setStatsServerId(donkeyConfiguration.getServerId());

        if (StringUtils.equalsIgnoreCase(pool, "DBCP")) {
            logger.debug("Initializing DBCP");
            jdbcDaoFactory.setConnectionPool(new DBCPConnectionPool(url, username, password, maxConnections, readOnly));
        } else {
            logger.debug("Initializing HikariCP");
            jdbcDaoFactory.setConnectionPool(new HikariConnectionPool(driver, url, username, password, maxConnections, jdbc4, testQuery, readOnly));
        }

        jdbcDaoFactory.setSerializerProvider(serializerProvider);
        jdbcDaoFactory.setQuerySource(xmlQuerySource);

        return jdbcDaoFactory;
    }

    public void startEngine() throws StartException {
        DonkeyDao dao = null;
        try {
            dao = daoFactory.getDao();

            if (dao.initTableStructure()) {
                dao.commit();
            }

            dao.checkAndCreateChannelTables();
            dao.commit();
        } catch (Exception e) {
            logger.error("Count not check and create channel tables on startup", e);
        } finally {
            if (dao != null) {
                dao.close();
            }
        }

        // load channel statistics into memory
        ChannelController.getInstance().loadStatistics(donkeyConfiguration.getServerId());

        encryptor = donkeyConfiguration.getEncryptor();

        eventDispatcher = donkeyConfiguration.getEventDispatcher();

        int updateInterval = NumberUtils.toInt(donkeyConfiguration.getDonkeyProperties().getProperty("donkey.statsupdateinterval"), DonkeyStatisticsUpdater.DEFAULT_UPDATE_INTERVAL);
        statisticsUpdater = new DonkeyStatisticsUpdater(daoFactory, updateInterval);
        statisticsUpdater.start();

        running = true;
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public DonkeyDaoFactory getReadOnlyDaoFactory() {
        return readOnlyDaoFactory;
    }

    public void setReadOnlyDaoFactory(DonkeyDaoFactory readOnlyDaoFactory) {
        this.readOnlyDaoFactory = readOnlyDaoFactory;
    }

    public DonkeyStatisticsUpdater getStatisticsUpdater() {
        return statisticsUpdater;
    }

    public void stopEngine() {
        if (statisticsUpdater != null) {
            statisticsUpdater.shutdown();
        }

        running = false;
    }

    public Map<String, Channel> getDeployedChannels() {
        return deployedChannels;
    }

    public Set<String> getDeployedChannelIds() {
        List<Channel> channels = new ArrayList<Channel>(deployedChannels.values());

        Collections.sort(channels, new Comparator<Channel>() {

            @Override
            public int compare(Channel o1, Channel o2) {
                return o1.getDeployDate().compareTo(o2.getDeployDate());
            }

        });

        Set<String> channelIds = new LinkedHashSet<String>();

        for (Channel channel : channels) {
            channelIds.add(channel.getChannelId());
        }

        return channelIds;
    }

    public boolean isRunning() {
        return running;
    }

    public DonkeyConfiguration getConfiguration() {
        return donkeyConfiguration;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
}
