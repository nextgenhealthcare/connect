package com.mirth.connect.donkey.util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DummyJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {}
}