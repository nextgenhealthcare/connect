package com.webreach.mirth.plugins.databasepruner;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class DatabasePrunerService implements ServerPlugin
{
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelController channelController = new ChannelController();
    private MessageObjectController messageObjectController = MessageObjectController.getInstance();
    private int sleepInterval;
    private Thread pruner;

    public void init(Properties properties)
    {
        pruner = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        pruneDatabase();
                        Thread.sleep(sleepInterval);
                    }
                }
                catch (InterruptedException e)
                {
                    logger.debug("exiting database pruner");
                }
            }
        });

        pruner.setName(properties.getProperty("name"));
        sleepInterval = Integer.valueOf(properties.getProperty("sleepInterval")) * 1000;
    }

    public void start()
    {
        pruner.start();
    }

    public void update(Properties properties)
    {
        sleepInterval = Integer.valueOf(properties.getProperty("sleepInterval"));
    }

    public void stop()
    {
        pruner.interrupt();

    }

    public Properties getDefaultProperties()
    {
        Properties properties = new Properties();
        properties.put("name", "Database Pruner");
        properties.put("sleepInterval", "300"); // 5 minutes
        return properties;
    }

    public void pruneDatabase()
    {
        logger.debug("pruning database");

        try
        {
            List<Channel> channels = channelController.getChannel(null);

            for (Iterator iter = channels.iterator(); iter.hasNext();)
            {
                Channel channel = (Channel) iter.next();

                if ((channel.getProperties().getProperty("store_messages") != null) && channel.getProperties().getProperty("store_messages").equals("true"))
                {
                    if ((channel.getProperties().getProperty("max_message_age") != null) && !channel.getProperties().getProperty("max_message_age").equals("-1"))
                    {
                        int numDays = Integer.parseInt(channel.getProperties().getProperty("max_message_age"));

                        Calendar endDate = Calendar.getInstance();
                        endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) - numDays);

                        MessageObjectFilter filter = new MessageObjectFilter();
                        filter.setChannelId(channel.getId());
                        filter.setEndDate(endDate);
                        messageObjectController.removeMessages(filter);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("could not prune database", e);
        }
    }
}
