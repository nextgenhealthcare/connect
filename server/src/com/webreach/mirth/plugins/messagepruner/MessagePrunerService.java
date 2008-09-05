package com.webreach.mirth.plugins.messagepruner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.text.DateFormatter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.util.PropertyLoader;

public class MessagePrunerService implements ServerPlugin, Job
{
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private Scheduler sched = null;
    private SchedulerFactory schedFact = null;
    private JobDetail jobDetail = null;
    private static LinkedList<String[]> log;
    private static final int LOG_SIZE = 250;
        
    public void init(Properties properties)
    {
        jobDetail = new JobDetail("prunerJob", Scheduler.DEFAULT_GROUP, MessagePrunerService.class);
        
        try
        {
            schedFact = new StdSchedulerFactory();
            sched = schedFact.getScheduler();
            sched.scheduleJob(jobDetail, createTrigger(properties));
            log = new LinkedList<String[]>();
        }
        catch (Exception e)
        {
            logger.error("error encountered in database pruner initialization", e);
        }
    }
    
    private Trigger createTrigger(Properties properties) throws ParseException
    {
        Trigger trigger = null;
        String interval = PropertyLoader.getProperty(properties, "interval");
        
        if(interval.equals("hourly"))
            trigger = TriggerUtils.makeHourlyTrigger();
        else 
        {    
            SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
            DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
            
            String time = PropertyLoader.getProperty(properties, "time");
            Date timeDate = (Date)timeFormatter.stringToValue(time);
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(timeDate);
            
            if(interval.equals("daily"))
            {
                trigger = TriggerUtils.makeDailyTrigger(timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
            }
            else if(interval.equals("weekly"))
            {
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("EEEEEEEE");
                DateFormatter dayFormatter = new DateFormatter(dayDateFormat);
                
                String dayOfWeek = PropertyLoader.getProperty(properties, "dayOfWeek");
                Date dayDate = (Date)dayFormatter.stringToValue(dayOfWeek);
                Calendar dayCalendar = Calendar.getInstance();
                dayCalendar.setTime(dayDate);
                
                trigger = TriggerUtils.makeWeeklyTrigger(dayCalendar.get(Calendar.DAY_OF_WEEK), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
            }
            else if(interval.equals("monthly"))
            {
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("DD");
                DateFormatter dayFormatter = new DateFormatter(dayDateFormat);
                
                String dayOfMonth = PropertyLoader.getProperty(properties, "dayOfMonth");
                Date dayDate = (Date)dayFormatter.stringToValue(dayOfMonth);
                Calendar dayCalendar = Calendar.getInstance();
                dayCalendar.setTime(dayDate);
                
                trigger = TriggerUtils.makeMonthlyTrigger(dayCalendar.get(Calendar.DAY_OF_MONTH), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
            }
        }
            
        trigger.setStartTime(new Date());
        trigger.setName("prunerTrigger");
        trigger.setJobName("prunerJob");
        return trigger;
    }

    public void start()
    {
        try
        {
            sched.start();
        }
        catch (Exception e)
        {
            logger.error("could not start message pruner", e);
        }
    }

    public void update(Properties properties)
    {
        try
        {
            sched.deleteJob("prunerJob", Scheduler.DEFAULT_GROUP);
            sched.scheduleJob(jobDetail, createTrigger(properties));
            
            // for some reason, this does not work
            //sched.rescheduleJob("prunerJob", Scheduler.DEFAULT_GROUP, createTrigger(properties));
        }
        catch (Exception e)
        {
            logger.error("could not reschedule the message pruner", e);
        }
    }
    
    public void onDeploy()
    {
        // TODO Auto-generated method stub
        
    }

    public void stop()
    {
        try
        {
            sched.shutdown();
        }
        catch (Exception e)
        {
            logger.error("could not exit message pruner", e);
        }
    }

    public Object invoke(String method, Object object, String sessionId)
    {
        if(method.equals("getLog"))
        {
            return (Object) getLog();
        }
        
        return null;
    }
    
    private List<String[]> getLog()
    {
        return log;
    }

    public Properties getDefaultProperties()
    {
        Properties properties = new Properties();
        properties.put("name", "Message Pruner");
        properties.put("interval", "hourly"); // 5 minutes
        return properties;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        logger.debug("pruning message database");

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
                        
                        int result = ControllerFactory.getFactory().createMessageObjectController().removeMessages(filter);
                        
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(calendar.getTime());
                          
                        String channelName = channel.getName();
                        String date = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", calendar);
                        String numberRemoved = String.valueOf(result);
                        
                        if(log.size() == LOG_SIZE)
                            log.removeLast();
                        
                        log.addFirst(new String[]{channelName, date, numberRemoved});
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("could not prune message database", e);
        }
    }
}
