/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("driverInfo")
public class DriverInfo implements Serializable {
    private String className;
    private String name;
    private String template;
    private String selectLimit;
    private List<String> alternativeClassNames;

    public DriverInfo() {

    }

    public DriverInfo(String name, String className, String template, String selectLimit) {
        this(name, className, template, selectLimit, new ArrayList<String>());
    }

    public DriverInfo(String name, String className, String template, String selectLimit, List<String> alternativeClassNames) {
        this.name = name;
        this.className = className;
        this.template = template;
        this.selectLimit = selectLimit;
        this.alternativeClassNames = alternativeClassNames;
    }

    public static List<DriverInfo> getDefaultDrivers() {
        List<DriverInfo> drivers = new ArrayList<DriverInfo>();

        drivers.add(new DriverInfo("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://host:port/dbname", "SELECT * FROM ? LIMIT 1", new ArrayList<String>(Arrays.asList(new String[] {
                "com.mysql.jdbc.Driver" }))));
        drivers.add(new DriverInfo("Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@host:port:dbname", "SELECT * FROM ? WHERE ROWNUM < 2"));
        drivers.add(new DriverInfo("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://host:port/dbname", "SELECT * FROM ? LIMIT 1"));
        drivers.add(new DriverInfo("SQL Server/Sybase (jTDS)", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://host:port/dbname", "SELECT TOP 1 * FROM ?"));
        drivers.add(new DriverInfo("Microsoft SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://host:port;databaseName=dbname", "SELECT TOP 1 * FROM ?"));
        drivers.add(new DriverInfo("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:dbfile.db", "SELECT * FROM ? LIMIT 1"));

        return drivers;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(String selectLimit) {
        this.selectLimit = selectLimit;
    }

    public List<String> getAlternativeClassNames() {
        return alternativeClassNames;
    }

    public void setAlternativeClassNames(List<String> alternativeClassNames) {
        this.alternativeClassNames = alternativeClassNames;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName() + "[");
        builder.append("name=" + getName() + ", ");
        builder.append("className=" + getClassName() + ", ");
        builder.append("template=" + getTemplate() + ", ");
        builder.append("selectLimit=" + getSelectLimit() + ", ");
        builder.append("alternativeClassNames=" + getAlternativeClassNames());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
