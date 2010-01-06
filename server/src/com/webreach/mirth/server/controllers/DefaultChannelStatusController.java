/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.util.JMXConnection;
import com.webreach.mirth.server.util.JMXConnectionFactory;

public class DefaultChannelStatusController extends ChannelStatusController {
    private Logger logger = Logger.getLogger(this.getClass());
    private EventController systemLogger = ControllerFactory.getFactory().createEventController();

    private static DefaultChannelStatusController instance = null;

    private DefaultChannelStatusController() {

    }

    public static ChannelStatusController create() {
        synchronized (DefaultChannelStatusController.class) {
            if (instance == null) {
                instance = new DefaultChannelStatusController();
            }

            return instance;
        }
    }

    /**
     * Starts the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public void startChannel(String channelId) throws ControllerException {
        logger.debug("starting channel: " + channelId);

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("type", "control");
            properties.put("name", "ModelService");
            String[] params = { channelId };
            String[] signature = { "java.lang.String" };
            jmxConnection.invokeOperation(properties, "startComponent", params, signature);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            jmxConnection.close();
        }

        SystemEvent systemEvent = new SystemEvent("Channel started.");
        systemEvent.getAttributes().put("channelId", channelId);
        systemLogger.logSystemEvent(systemEvent);
    }

    /**
     * Stops the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public void stopChannel(String channelId) throws ControllerException {
        logger.debug("stopping channel: channelId=" + channelId);

        // if paused, must be resumed before stopped
        if (getState(channelId).equals(ChannelStatus.State.PAUSED)) {
            logger.debug("channel is paused, must resume before stopping");
            resumeChannel(channelId);
        }

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("type", "control");
            properties.put("name", "ModelService");
            String[] params = { channelId };
            String[] signature = { "java.lang.String" };
            jmxConnection.invokeOperation(properties, "stopComponent", params, signature);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            jmxConnection.close();
        }

        SystemEvent systemEvent = new SystemEvent("Channel stopped.");
        systemEvent.getAttributes().put("channelId", channelId);
        systemLogger.logSystemEvent(systemEvent);
    }

    /**
     * Pauses the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public void pauseChannel(String channelId) throws ControllerException {
        logger.debug("pausing channel: channelId=" + channelId);

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("type", "control");
            properties.put("name", "ModelService");
            String[] params = { channelId };
            String[] signature = { "java.lang.String" };
            jmxConnection.invokeOperation(properties, "pauseComponent", params, signature);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            jmxConnection.close();
        }

        SystemEvent systemEvent = new SystemEvent("Channel paused.");
        systemEvent.getAttributes().put("channelId", channelId);
        systemLogger.logSystemEvent(systemEvent);
    }

    /**
     * Resumes the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public void resumeChannel(String channelId) throws ControllerException {
        logger.debug("resuming channel: channelId=" + channelId);

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("type", "control");
            properties.put("name", "ModelService");
            String[] params = { channelId };
            String[] signature = { "java.lang.String" };
            jmxConnection.invokeOperation(properties, "resumeComponent", params, signature);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            jmxConnection.close();
        }

        SystemEvent systemEvent = new SystemEvent("Channel resumed.");
        systemEvent.getAttributes().put("channelId", channelId);
        systemLogger.logSystemEvent(systemEvent);
    }

    /**
     * Returns a list of ChannelStatus objects representing the running
     * channels.
     * 
     * @return
     * @throws ControllerException
     */
    public List<ChannelStatus> getChannelStatusList() {
        logger.debug("retrieving channel status list");
        List<ChannelStatus> channelStatusList = new ArrayList<ChannelStatus>();

        try {
            for (String channelId : getDeployedIds()) {
                ChannelStatus channelStatus = new ChannelStatus();
                channelStatus.setChannelId(channelId);

                // check if the channel is running but has been removed from the
                // channel list
                HashMap<String, Channel> channelCache = ControllerFactory.getFactory().createChannelController().getChannelCache();

                if ((channelCache != null) && channelCache.containsKey(channelId)) {
                    Channel cachedChannel = channelCache.get(channelId);
                    channelStatus.setName(cachedChannel.getName());
                } else {
                    channelStatus.setName("Channel has been deleted.");
                }

                channelStatus.setState(getState(channelId));
                channelStatusList.add(channelStatus);
            }

            return channelStatusList;
        } catch (Exception e) {
            logger.debug("could not retrieve channel status list");
            // returns an empty list
            return channelStatusList;
        }
    }

    /**
     * Returns a list of the ids of all running channels.
     * 
     * @return
     * @throws ControllerException
     */
    private List<String> getDeployedIds() throws ControllerException {
        logger.debug("retrieving deployed channel id list");
        List<String> deployedChannelIdList = new ArrayList<String>();

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Set<ObjectName> beanObjectNames = jmxConnection.getMBeanNames();

            for (ObjectName objectName : beanObjectNames) {
                // only add valid mirth channels
                // format: MirthConfiguration:type=statistics,name=*
                // We also don't want the "sink" channel showing up.
                // Nor components which aren't registered
                if ((objectName.getKeyProperty("type") != null)
                        && objectName.getKeyProperty("type").equals("statistics")
                        && (objectName.getKeyProperty("name") != null)
                        && !objectName.getKeyProperty("name").startsWith("_")
                        && !objectName.getKeyProperty("name").equals("MessageSink") && (objectName.getKeyProperty("router") == null)
                        && isComponentRegistered(objectName.getKeyProperty("name"))) {
                    deployedChannelIdList.add(objectName.getKeyProperty("name"));
                }
            }

            return deployedChannelIdList;
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            // to prevent closing the connection when the server is restarting
            if (jmxConnection != null) {
                jmxConnection.close();
            }
        }
    }

    /**
     * Returns the state of a channel with the specified id.
     * 
     * @param channelId
     * @return
     * @throws ControllerException
     */
    private ChannelStatus.State getState(String channelId) throws ControllerException {
        logger.debug("retrieving channel state: channelId=" + channelId);

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("type", "control");
            properties.put("name", channelId + "ComponentService");

            if ((Boolean) jmxConnection.getAttribute(properties, "Paused")) {
                return ChannelStatus.State.PAUSED;
            } else if ((Boolean) jmxConnection.getAttribute(properties, "Stopped")) {
                return ChannelStatus.State.STOPPED;
            } else {
                return ChannelStatus.State.STARTED;
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            jmxConnection.close();
        }
    }

    private boolean isComponentRegistered(String id) throws ControllerException {
        logger.debug("checking component registration state: id=" + id);

        JMXConnection jmxConnection = null;

        try {
            jmxConnection = JMXConnectionFactory.createJMXConnection();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("type", "control");
            properties.put("name", "ModelService");
            String[] params = { id };
            String[] signature = { "java.lang.String" };
            return (Boolean) jmxConnection.invokeOperation(properties, "isComponentRegistered", params, signature);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            jmxConnection.close();
        }
    }
}
