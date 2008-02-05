package com.webreach.mirth.plugins;

import java.util.Calendar;
import java.util.Properties;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.connectors.file.FileWriterProperties;
import com.webreach.mirth.connectors.mllp.LLPListenerProperties;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Connector.Mode;
import com.webreach.mirth.model.MessageObject.Protocol;

public abstract class ChannelWizardPlugin extends ClientPlugin {
	
	private Frame parent = PlatformUI.MIRTH_FRAME;
	
    public abstract Channel runWizard();
    
    public ChannelWizardPlugin(String name)
    {
        super(name);
    }
    
    public void alertException(StackTraceElement[] strace, String message)
    {
        parent.alertException(strace, message);
    }
    
    public void alertWarning(String message)
    {
        parent.alertWarning(message);
    }
    
    public void alertInformation(String message)
    {
        parent.alertInformation(message);
    }
    
    public void alertError(String message)
    {
        parent.alertError(message);
    }
    
    public boolean alertOkCancel(String message)
    {
        return parent.alertOkCancel(message);
    }
    
    public boolean alertOption(String message)
    {
        return parent.alertOption(message);
    }
    
    public Channel getDefaultNewChannel() {
    	Channel channel = new Channel();
    	
    	channel.setName("New Channel");
    	channel.setEnabled(true);
        channel.getProperties().setProperty("initialState", "Started");
        channel.setLastModified(Calendar.getInstance());
		
        Connector sourceConnector = new Connector();
		sourceConnector.setEnabled(true);
		sourceConnector.setFilter(new Filter());
		Transformer sourceTransformer = new Transformer();
		sourceTransformer.setInboundProtocol(Protocol.HL7V2);
		sourceConnector.setTransformer(sourceTransformer);
		sourceConnector.setMode(Mode.SOURCE);
		sourceConnector.setName("sourceConnector");
		sourceConnector.setTransportName(LLPListenerProperties.name);
		Properties sourceProperties = new LLPListenerProperties().getDefaults();
		sourceConnector.setProperties(sourceProperties);
		channel.setSourceConnector(sourceConnector);
		
        Connector destinationConnector = new Connector();
        destinationConnector.setEnabled(true);
        destinationConnector.setFilter(new Filter());
        destinationConnector.setTransformer(new Transformer());
        destinationConnector.setMode(Mode.DESTINATION);
        destinationConnector.setName("Destination 1");
        destinationConnector.setTransportName(FileWriterProperties.name);
		Properties destinationConnectorProperties = new FileWriterProperties().getDefaults();
		destinationConnector.setProperties(destinationConnectorProperties);
		channel.getDestinationConnectors().add(destinationConnector);		
                
    	return channel;
    }
}
