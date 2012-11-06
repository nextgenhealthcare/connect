package com.mirth.connect.donkey.server.data.passthru;

import com.mirth.connect.donkey.server.channel.Statistics;

public interface StatisticsUpdater {
    public void update(Statistics statistics);
}
