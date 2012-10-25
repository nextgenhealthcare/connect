/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

public class ActionTimer {
    private boolean enabled = true;
    private Map<String, Long> times = new ConcurrentHashMap<String, Long>();
    private Map<String, Integer> counts = new ConcurrentHashMap<String, Integer>();

    public void log(String eventName, long millis) {
        if (!enabled) {
            return;
        }

        Long oldTime = times.get(eventName);

        if (oldTime == null) {
            times.put(eventName, millis);
        } else {
            times.put(eventName, oldTime + millis);
        }

        Integer count = counts.get(eventName);

        if (count == null) {
            counts.put(eventName, 1);
        } else {
            counts.put(eventName, count + 1);
        }
    }

    public long getTotalTime() {
        long total = 0;

        for (Long time : times.values()) {
            total += time;
        }

        return total;
    }

    public int getTotalCount() {
        int total = 0;

        for (Integer count : counts.values()) {
            total += count;
        }

        return total;
    }

    public Map<String, Long> getTimes() {
        return times;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public String getLog() {
        long totalTime = getTotalTime();

        StringBuilder log = new StringBuilder();
        LongComparator longComparator = new LongComparator(times);
        TreeMap<String, Long> sortedTimes = new TreeMap<String, Long>(longComparator);
        sortedTimes.putAll(times);

        for (Entry<String, Long> logEntry : sortedTimes.entrySet()) {
            String eventName = logEntry.getKey();
            Integer count = counts.get(eventName);
            Long time = logEntry.getValue();

            long pct = (totalTime > 0) ? ((time * 100) / totalTime) : 0;
            log.append(StringUtils.rightPad(eventName + ":", 40) + StringUtils.rightPad(count + " occurrence" + ((count != 1) ? "s" : ""), 20) + StringUtils.rightPad(time + "ms", 12) + pct + "%\n");
        }

        int totalCount = getTotalCount();
        log.append(StringUtils.rightPad("Totals:", 40) + StringUtils.rightPad(totalCount + " occurrence" + ((totalCount != 1) ? "s" : ""), 20) + totalTime + "ms\n");
        return log.toString();
    }

    public void reset() {
        counts.clear();
        times.clear();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private class LongComparator implements Comparator<String> {
        private Map<String, Long> base;

        public LongComparator(Map<String, Long> base) {
            this.base = base;
        }

        @Override
        public int compare(String a, String b) {
            return base.get(b).compareTo(base.get(a));
        }
    }
}
