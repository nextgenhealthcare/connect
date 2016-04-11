/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.DatabaseMetaData;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.servlets.SystemServletInterface;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.model.SystemInfo;
import com.mirth.connect.model.SystemStats;
import com.mirth.connect.server.api.MirthServlet;

public class SystemServlet extends MirthServlet implements SystemServletInterface {
    public SystemServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, false);
    }

    @Override
    public SystemInfo getInfo() throws ClientException {
        try {
            // TODO this isn't very safe, it assumes that the DaoFactory is always a JdbcDaoFactory
            JdbcDao dao = (JdbcDao) Donkey.getInstance().getDaoFactory().getDao();

            try {
                DatabaseMetaData metaData = dao.getConnection().getMetaData();

                SystemInfo info = new SystemInfo();
                info.setDbName(metaData.getDatabaseProductName());
                info.setDbVersion(metaData.getDatabaseProductVersion());
                info.setJvmVersion(System.getProperty("java.version"));
                info.setOsName(System.getProperty("os.name"));
                info.setOsVersion(System.getProperty("os.version"));
                info.setOsArchitecture(System.getProperty("os.arch"));
                return info;
            } finally {
                dao.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public SystemStats getStats() throws ClientException {
        SystemStats stats = new SystemStats();
        stats.setTimestamp(Calendar.getInstance());

        stats.setFreeMemoryBytes(Runtime.getRuntime().freeMemory());
        stats.setAllocatedMemoryBytes(Runtime.getRuntime().totalMemory());
        stats.setMaxMemoryBytes(Runtime.getRuntime().maxMemory());

        // TODO handle systems with multiple file-system roots
        File[] roots = File.listRoots();
        stats.setDiskFreeBytes(roots[0].getFreeSpace());
        stats.setDiskTotalBytes(roots[0].getTotalSpace());

        com.sun.management.OperatingSystemMXBean osMxBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        stats.setCpuUsagePct(osMxBean.getProcessCpuLoad());

        return stats;
    }
}
