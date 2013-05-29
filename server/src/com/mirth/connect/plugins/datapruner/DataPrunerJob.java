/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DataPrunerJob implements Job {
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        DataPrunerService.pruner.start();
    }
}
