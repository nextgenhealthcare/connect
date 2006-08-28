package org.mule.management.stats;

import java.io.PrintWriter;

import org.mule.management.stats.printers.SimplePrinter;

import com.webreach.mirth.server.controllers.ChannelStatisticsController;

/**
 * <code>ComponentStatistics</code> is used for capturing compenet event
 * processing statistics that can be exposed via management services such as
 * JMX.
 * 
 * @author <a href="mailto:S.Vanmeerhaege@gfdi.be">Vanmeerhaeghe Stéphane </a>
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason </a>
 * @version $Revision: 1.3 $
 */
public class ComponentStatistics implements Statistics
{
    private String name;
    private long totalExecTime = 0;
    private long receivedEventSync = 0;
    private long receivedEventASync = 0;
    private long queuedEvent = 0;
    private long maxQueuedEvent = 0;
    private long averageQueueSize = 0;
    private long totalQueuedEvent = 0;
    private long sentEventSync = 0;
    private long sentReplyToEvent = 0;
    private long sentEventASync = 0;
    private long executedEvent = 0;
    private long executionError = 0;
    private long fatalError = 0;
    private long minExecutionTime = 0;
    private long maxExecutionTime = 0;
    private long averageExecutionTime = 0;
    private boolean enabled = false;
    private int componentPoolMaxSize = 0;
    private int componentPoolAbsoluteMaxSize = 0;
    private int componentPoolSize = 0;
    private int threadPoolSize = 0;
    private long samplePeriod = 0;

    private RouterStatistics inboundRouterStat = null;
    private RouterStatistics outboundRouterStat = null;
    
    private ChannelStatisticsController controller = new ChannelStatisticsController();

    /**
     * 
     * The constructor
     * 
     * @param name
     */
    public ComponentStatistics(String name, int componentPoolsize, int threadPoolSize)
    {
        super();
        this.name = name;
        this.componentPoolMaxSize = componentPoolsize;
        this.componentPoolAbsoluteMaxSize = componentPoolMaxSize;
        this.threadPoolSize = threadPoolSize;
        clear();
    }

    /**
     * Are statistics logged
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Enable statistics logs (this is a dynamic parameter)
     */
    public synchronized void setEnabled(boolean b)
    {
        enabled = b;

        if (inboundRouterStat != null) {
            inboundRouterStat.setEnabled(b);
        }
        if (outboundRouterStat != null) {
            outboundRouterStat.setEnabled(b);
        }
    }

    public synchronized void incReceivedEventSync()
    {
    	// added to update stats
    	try {
    		controller.incReceivedCount(Integer.parseInt(name));	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        receivedEventSync++;
    }

    public synchronized void incReceivedEventASync()
    {
    	// added to update stats
    	try {
    		controller.incReceivedCount(Integer.parseInt(name));	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        receivedEventASync++;
    }

    public synchronized void incExecutionError()
    {
    	// added to update stats
    	try {
    		controller.incErrorCount(Integer.parseInt(name));	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        executionError++;
    }

    public synchronized void incFatalError()
    {
    	// added to update stats
    	try {
    		controller.incErrorCount(Integer.parseInt(name));	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        fatalError++;
    }

    public synchronized void incSentEventSync()
    {
    	// added to update stats
    	try {
    		controller.incSentCount(Integer.parseInt(name));	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        sentEventSync++;
    }

    public synchronized void incSentEventASync()
    {
    	// added to update stats
    	try {
    		controller.incSentCount(Integer.parseInt(name));	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        sentEventASync++;
    }

    public synchronized void incSentReplyToEvent()
    {
        sentReplyToEvent++;
    }

    public synchronized void incQueuedEvent()
    {
        queuedEvent++;
        totalQueuedEvent++;
        if (queuedEvent > maxQueuedEvent) {
            maxQueuedEvent = queuedEvent;
        }
        // if(queuedEvent > 1) {
        averageQueueSize = Math.round(getAsyncEventsReceived() / totalQueuedEvent);
        // }
    }

    public synchronized void decQueuedEvent()
    {
        queuedEvent--;
    }

    public synchronized void addExecutionTime(long time)
    {
        executedEvent++;

        totalExecTime += (time == 0 ? 1 : time);

        if (minExecutionTime == 0 || time < minExecutionTime) {
            minExecutionTime = time;
        }
        if (maxExecutionTime == 0 || time > maxExecutionTime) {
            maxExecutionTime = time;
        }
        averageExecutionTime = Math.round(totalExecTime / executedEvent);
    }

    public long getAverageExecutionTime()
    {
        return averageExecutionTime;
    }

    public long getAverageQueueSize()
    {
        return averageQueueSize;
    }

    public long getMaxQueueSize()
    {
        return maxQueuedEvent;
    }

    public long getMaxExecutionTime()
    {
        return maxExecutionTime;
    }

    public long getFatalErrors()
    {
        return fatalError;
    }

    public long getMinExecutionTime()
    {
        return minExecutionTime;
    }

    public long getTotalExecutionTime()
    {
        return totalExecTime;
    }

    public long getQueuedEvents()
    {
        return queuedEvent;
    }

    public long getAsyncEventsReceived()
    {
        return receivedEventASync;
    }

    public long getSyncEventsReceived()
    {
        return receivedEventSync;
    }

    public long getReplyToEventsSent()
    {
        return sentReplyToEvent;
    }

    public long getSyncEventsSent()
    {
        return sentEventSync;
    }

    public long getAsyncEventsSent()
    {
        return sentEventASync;
    }

    public long getTotalEventsSent()
    {
        return getSyncEventsSent() + getAsyncEventsSent();
    }

    public long getTotalEventsReceived()
    {
        return getSyncEventsReceived() + getAsyncEventsReceived();
    }

    public long getExecutedEvents()
    {
        return executedEvent;
    }

    public long getExecutionErrors()
    {
        return executionError;
    }

    public String getName()
    {
        return name;
    }

    public synchronized void setName(String name)
    {
        this.name = name;
    }

    /**
     * log in info level the main statistics
     */
    public void logSummary()
    {
        logSummary(new SimplePrinter(System.out));
    }

    public void logSummary(PrintWriter printer)
    {
        printer.print(this);
    }

    public synchronized void clear()
    {

        componentPoolSize = 0;
        componentPoolAbsoluteMaxSize = 0;
        totalExecTime = 0;
        receivedEventSync = 0;
        receivedEventASync = 0;
        queuedEvent = 0;
        maxQueuedEvent = 0;
        totalQueuedEvent = 0;
        averageQueueSize = 0;

        sentEventSync = 0;
        sentEventASync = 0;
        sentReplyToEvent = 0;

        executedEvent = 0;
        executionError = 0;
        fatalError = 0;

        minExecutionTime = 0;
        maxExecutionTime = 0;

        if (getInboundRouterStat() != null) {
            getInboundRouterStat().clear();
        }
        if (getOutboundRouterStat() != null) {
            getOutboundRouterStat().clear();
        }

        samplePeriod = System.currentTimeMillis();

    }

    /**
     * @return Returns the inboundRouterStat.
     */
    public RouterStatistics getInboundRouterStat()
    {
        return inboundRouterStat;
    }

    /**
     * @param inboundRouterStat The inboundRouterStat to set.
     */
    public void setInboundRouterStat(RouterStatistics inboundRouterStat)
    {
        this.inboundRouterStat = inboundRouterStat;
        this.inboundRouterStat.setEnabled(enabled);
    }

    /**
     * @return Returns the outboundRouterStat.
     */
    public RouterStatistics getOutboundRouterStat()
    {
        return outboundRouterStat;
    }

    /**
     * @param outboundRouterStat The outboundRouterStat to set.
     */
    public void setOutboundRouterStat(RouterStatistics outboundRouterStat)
    {
        this.outboundRouterStat = outboundRouterStat;
        this.outboundRouterStat.setEnabled(enabled);
    }

    public int getComponentPoolMaxSize()
    {
        return componentPoolMaxSize;
    }

    public int getComponentPoolAbsoluteMaxSize()
    {
        return componentPoolAbsoluteMaxSize;
    }

    public int getComponentPoolSize()
    {
        return componentPoolSize;
    }

    public synchronized void setComponentPoolSize(int componentPoolSize)
    {
        this.componentPoolSize = componentPoolSize;
        if (componentPoolSize > componentPoolAbsoluteMaxSize) {
            componentPoolAbsoluteMaxSize = componentPoolSize;
        }
    }

    public int getThreadPoolSize()
    {
        return threadPoolSize;
    }

    public long getSamplePeriod()
    {
        return System.currentTimeMillis() - samplePeriod;
    }
}
