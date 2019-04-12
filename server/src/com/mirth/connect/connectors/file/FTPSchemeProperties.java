package com.mirth.connect.connectors.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class FTPSchemeProperties extends SchemeProperties {

    private List<String> commands;
    
    public FTPSchemeProperties() {
        commands = new ArrayList<>();
    }
    
    public FTPSchemeProperties(FTPSchemeProperties props) {
        commands = new ArrayList<>();
        for (String command: props.getCommands()) {
            commands.add(command);
        }
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public SchemeProperties getFileSchemeProperties() {
        return this;
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";
        
        if (!commands.isEmpty()) {
            builder.append("[INITIAL COMMANDS]");
            builder.append(newLine);
            
            for (String command: commands) {
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
        builder.append(String.join(",", commands));
        
        return builder.toString();
    }
    
    @Override
    public SchemeProperties clone() {
        return new FTPSchemeProperties(this);
    }
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<>();
        purgedProperties.put("initialCommandsCount", commands.size());
        return purgedProperties;
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
