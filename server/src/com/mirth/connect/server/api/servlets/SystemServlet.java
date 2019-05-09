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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.servlets.SystemServletInterface;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.model.SystemInfo;
import com.mirth.connect.model.SystemStats;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class SystemServlet extends MirthServlet implements SystemServletInterface {

    private static final Logger logger = Logger.getLogger(SystemServlet.class);
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

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

        File[] roots = File.listRoots();
        if (ArrayUtils.isNotEmpty(roots)) {
            // Default to the first root in the list
            File root = roots[0];

            // Windows systems have multiple roots, like "A:" and "C:".
            if (StringUtils.containsIgnoreCase(System.getProperty("os.name"), "Windows")) {
                try {
                    // Attempt to get the correct root from the Mirth Connect base directory
                    File baseDir = new File(configurationController.getBaseDir());
                    // Split on the file separator
                    String[] path = StringUtils.split(baseDir.getCanonicalPath(), File.separatorChar);
                    // The first part should be the root, e.g. "C:\"
                    File pathRoot = new File(path[0] + File.separatorChar);

                    // Make sure the candidate root exists
                    if (!pathRoot.exists()) {
                        throw new Exception("Root directory \"" + pathRoot + "\" does not exist.");
                    }

                    // Make sure the candidate root matches one of the roots reported by the JVM
                    boolean found = false;
                    for (File testRoot : roots) {
                        if (testRoot.equals(pathRoot)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new Exception("Root directory \"" + pathRoot + "\" exists, but does not match any of the filesystem roots reported by the JVM.");
                    }

                    root = pathRoot;
                } catch (Exception e) {
                    logger.warn("Unable to infer filesystem root from Mirth Connect base directory, defaulting to: " + root, e);
                }
            }

            stats.setDiskFreeBytes(root.getFreeSpace());
            stats.setDiskTotalBytes(root.getTotalSpace());
        }

        com.sun.management.OperatingSystemMXBean osMxBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        stats.setCpuUsagePct(osMxBean.getProcessCpuLoad());

        return stats;
    }
}
