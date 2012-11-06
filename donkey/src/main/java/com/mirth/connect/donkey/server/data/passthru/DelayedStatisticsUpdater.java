package com.mirth.connect.donkey.server.data.passthru;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class DelayedStatisticsUpdater implements StatisticsUpdater, Runnable {
    private DonkeyDaoFactory daoFactory;
    private Statistics statistics = new Statistics();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private int delayMillis = 5000;
    private boolean running = false;
    private Logger logger = Logger.getLogger(getClass());
    
    public DelayedStatisticsUpdater(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
    
    public int getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public synchronized void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
    
    @Override
    public synchronized void update(Statistics statistics) {
        this.statistics.update(statistics);

        if (!running) {
            running = true;
            executor.schedule(this, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            DonkeyDao dao = daoFactory.getDao();

            try {
                dao.addChannelStatistics(statistics);
                dao.commit();
                statistics.getStats().clear();
            } catch (Throwable t) {
                logger.error(t);
            } finally {
                running = false;
                dao.close();
            }
        }
    }
}
