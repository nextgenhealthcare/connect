package com.mirth.connect.connectors.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class FTPSchemeProperties extends SchemeProperties {

    private List<String> initialCommands;
    
    public FTPSchemeProperties() {
        initialCommands = new ArrayList<>();
    }
    
    public FTPSchemeProperties(FTPSchemeProperties props) {
        initialCommands = new ArrayList<>();
        
        List<String> propCommands = props.getInitialCommands();
        if (CollectionUtils.isNotEmpty(propCommands)) {
            for (String command: props.getInitialCommands()) {
                initialCommands.add(command);
            }
        }
    }
    
    public List<String> getInitialCommands() {
        return initialCommands;
    }
    
    public void setInitialCommands(List<String> commands) {
        this.initialCommands = commands;
    }

    @Override
    public SchemeProperties getFileSchemeProperties() {
        return this;
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";
        
        if (CollectionUtils.isNotEmpty(initialCommands)) {
            builder.append("[INITIAL COMMANDS]");
            builder.append(newLine);
            
            for (String command: initialCommands) {
                builder.append(command);
                builder.append(newLine);
            }
        }
        
        return builder.toString();
    }

    @Override
    public String getSummaryText() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Initial Commands: ");
        builder.append(String.join(",", initialCommands));
        
        return builder.toString();
    }
    
    @Override
    public SchemeProperties clone() {
        return new FTPSchemeProperties(this);
    }
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<>();
        purgedProperties.put("initialCommandsCount", initialCommands.size());
        return purgedProperties;
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
