/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("alertStatus")
public class AlertStatus implements Serializable {

    private String id;
    private String name;
    private boolean enabled;
    private Integer alertedCount;
    
    @XStreamOmitField
    private Logger logger = Logger.getLogger(getClass());

    public AlertStatus() {}
    
    public AlertStatus(String id, String name, boolean enabled, Integer alertedCount) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.alertedCount = alertedCount;
    }
    
    public AlertStatus(ResultSet resultSet) {
        try {
            id = resultSet.getString(1);
            name = resultSet.getString(2);
            enabled = resultSet.getBoolean(3);
            
            if (enabled) {
                alertedCount = resultSet.getInt(4);
            }
        } catch (SQLException e) {
            logger.error("Error instantiating AlertStatus from database ResultSet", e);
        }
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getAlertedCount() {
        return alertedCount;
    }

    public void setAlertedCount(Integer alertedCount) {
        this.alertedCount = alertedCount;
    }
    
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof AlertStatus)) {
            return false;
        }
        
        AlertStatus otherAlertStatus = (AlertStatus) otherObject; 
        
        if (!Objects.equals(otherAlertStatus.getId(), id)) {
            return false;
        }
        
        if (!Objects.equals(otherAlertStatus.getName(), name)) {
            return false;
        }
        
        if (otherAlertStatus.isEnabled() != enabled) {
            return false;
        }
        
        if (!Objects.equals(otherAlertStatus.getAlertedCount(), alertedCount)) {
            return false;
        }
        
        return true;
    }
    
}
