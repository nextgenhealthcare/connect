/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelLock;
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

    public void deployChannel(Channel channel) throws DeployException, StartException {
        try {
            // Locks the channel to prevent tasks other than Deploy or Start from happening during the deploy process.
            channel.lock(ChannelLock.DEPLOY);

            channel.setDeployDate(Calendar.getInstance());
            deployedChannels.put(channel.getChannelId(), channel);

            try {
                channel.deploy();
            } catch (DeployException e) {
                deployedChannels.remove(channel.getChannelId());
                throw e;
            }

            if (channel.getInitialState() == ChannelState.STARTED) {
                channel.start();
            } else if (channel.getInitialState() == ChannelState.PAUSED) {
                Set<Integer> connectorsToStart = new HashSet<Integer>(channel.getMetaDataIds());
                connectorsToStart.remove(0);
                channel.start(connectorsToStart);
            }
        } finally {
            channel.unlock();
        }
    }

    public void undeployChannel(String channelId) throws StopException, UndeployException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new StopException("Failed to find deployed channel id: " + channelId, null);
        }

        try {
            // Locks the channel to prevent tasks other than Stop or Undeploy from happening during the deploy process.
            // This prevents a channel Start task from being queued up while stop is running (possible if messages are being flushed)
            // which would start the channel again before the undeploy task runs.
            channel.lock(ChannelLock.UNDEPLOY);

            if (channel.isActive()) {
                try {
                    channel.stop();
                } catch (StopException e) {
                    //TODO cannot assume that channel is actually stopped after a StopException. Replace with different exception if halted?
                    logger.error(e);
                }
            }

            deployedChannels.remove(channelId);
            channel.undeploy();
        } finally {
            channel.unlock();
        }
    }

    public void startChannel(String channelId) throws StartException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new StartException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.start();
    }

    public void stopChannel(String channelId) throws StopException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new StopException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.stop();
    }

    public void haltChannel(String channelId) throws HaltException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new HaltException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.halt();
    }

    public void pauseChannel(String channelId) throws PauseException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new PauseException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.pause();
    }

    public void resumeChannel(String channelId) throws StartException, StopException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new StartException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.resume();
    }

    public void startConnector(String channelId, Integer metaDataId) throws StartException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new StartException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.startConnector(metaDataId);
    }

    public void stopConnector(String channelId, Integer metaDataId) throws StopException {
        Channel channel = deployedChannels.get(channelId);

        if (channel == null) {
            throw new StopException("Failed to find deployed channel id: " + channelId, null);
        }

        channel.stopConnector(metaDataId);
    }

    public Map<String, Channel> getDeployedChannels() {
        return deployedChannels;
    }

    public Set<String> getDeployedChannelIds() {
        TreeMap<Calendar, String> treeMap = new TreeMap<Calendar, String>(new Comparator<Calendar>() {

            @Override
            public int compare(Calendar o1, Calendar o2) {
                return o1.compareTo(o2);
            }

        });

        for (Channel channel : deployedChannels.values()) {
            treeMap.put(channel.getDeployDate(), channel.getChannelId());
        }

        return new LinkedHashSet<String>(treeMap.values());
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
