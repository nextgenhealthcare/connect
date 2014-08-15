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

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.DBCPConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.XmlQuerySource;
import com.mirth.connect.donkey.server.data.jdbc.XmlQuerySource.XmlQuerySourceException;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.xstream.XStreamSerializer;

public class Donkey {
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
    private Serializer serializer = new XStreamSerializer();
    private Encryptor encryptor;
    private EventDispatcher eventDispatcher;
    private Logger logger = Logger.getLogger(getClass());
    private boolean running = false;

    public void startEngine(DonkeyConfiguration donkeyConfiguration) throws StartException {
        this.donkeyConfiguration = donkeyConfiguration;

        initDaoFactory();

        // load channel statistics into memory
        ChannelController.getInstance().loadStatistics(donkeyConfiguration.getServerId());

        encryptor = donkeyConfiguration.getEncryptor();

        eventDispatcher = donkeyConfiguration.getEventDispatcher();

        running = true;
    }

    private void initDaoFactory() throws StartException {
        Properties dbProperties = donkeyConfiguration.getDatabaseProperties();
        String database = dbProperties.getProperty("database");
        String driver = dbProperties.getProperty("database.driver");
        String url = dbProperties.getProperty("database.url");
        String username = dbProperties.getProperty("database.username");
        String password = dbProperties.getProperty("database.password");
        int maxConnections;

        try {
            maxConnections = Integer.parseInt(dbProperties.getProperty("database.max-connections"));
        } catch (NumberFormatException e) {
            throw new StartException("Failed to read the database.max-connections configuration property");
        }

        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new StartException(e);
            }
        }

        JdbcDaoFactory jdbcDaoFactory = JdbcDaoFactory.getInstance(database);
        jdbcDaoFactory.setStatsServerId(donkeyConfiguration.getServerId());
        jdbcDaoFactory.setConnectionPool(new DBCPConnectionPool(url, username, password, maxConnections));
        jdbcDaoFactory.setSerializer(serializer);

        XmlQuerySource xmlQuerySource = new XmlQuerySource();

        try {
            xmlQuerySource.load("default.xml");
            xmlQuerySource.load(dbProperties.getProperty("database") + ".xml");
        } catch (XmlQuerySourceException e) {
            throw new StartException(e);
        }

        jdbcDaoFactory.setQuerySource(xmlQuerySource);

        JdbcDao dao = jdbcDaoFactory.getDao();

        try {
            if (dao.initTableStructure()) {
                dao.commit();
            }
        } finally {
            dao.close();
        }

        daoFactory = jdbcDaoFactory;
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public void stopEngine() {
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
