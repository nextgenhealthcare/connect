package com.mirth.connect.connectors.mongo;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;

public class MongoDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {


	private static final long serialVersionUID = -3865724302179322649L;
	private DestinationConnectorProperties destinationConnectorProperties;
	private String collectionName;
	private String dbUserName;
	private String dbPassword;
	private String dbName;
	private int dbPort=27017;
	private String hostAddress="localhost";
	

	public MongoDispatcherProperties() {
    	destinationConnectorProperties = new DestinationConnectorProperties();
    	//TODO:set defaults
	}
    public MongoDispatcherProperties(MongoDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

 
    }
    
	  @Override
	    public DestinationConnectorProperties getDestinationConnectorProperties() {
	        return destinationConnectorProperties;
	    }

	    @Override
	    public ConnectorProperties clone() {
	        return new MongoDispatcherProperties(this);
	    }

	    @Override
	    public boolean canValidateResponse() {
	        return false;
	    }

	    @Override
	    public boolean equals(Object obj) {
	        return EqualsBuilder.reflectionEquals(this, obj);
	    }

	    // @formatter:off
	    @Override public void migrate3_0_1(DonkeyElement element) {}
	    @Override public void migrate3_0_2(DonkeyElement element) {} // @formatter:on

	    @Override
	    public void migrate3_1_0(DonkeyElement element) {
	        super.migrate3_1_0(element);
	    }

	    @Override
	    public void migrate3_2_0(DonkeyElement element) {}

	    @Override
	    public void migrate3_3_0(DonkeyElement element) {}

	    @Override
	    public void migrate3_4_0(DonkeyElement element) {

	    }

	    // @formatter:off
	    @Override public void migrate3_5_0(DonkeyElement element) {}
	    @Override public void migrate3_6_0(DonkeyElement element) {}
	    @Override public void migrate3_7_0(DonkeyElement element) {} // @formatter:on

	    @Override
	    public Map<String, Object> getPurgedProperties() {
	        Map<String, Object> purgedProperties = super.getPurgedProperties();
//	        purgedProperties.put("documentType", documentType);
	        return purgedProperties;
	    }

	    @Override
	    public String getProtocol() {
	        return "mongo";
	    }

	    @Override
	    public String getName() {
	        return "MongoDB Writer";
	    }

	    @Override
	    public String toFormattedString() {
	       return this.toString();
	    }
	    
	    
		public String getCollectionName() {			
			return collectionName;
		}
		public void setCollectionName(String collectionName) {
			this.collectionName = collectionName;
		}
		public String getDbUserName() {
			
			return dbUserName;
		}
		public String getDbPassword() {
			return dbPassword;
		}
		public String getDbName() {
			return dbName;
		}
		public int getDbPort() {
			return dbPort;
		}
		
	    public void setDbUserName(String dbUserName) {
			this.dbUserName = dbUserName;
		}
		public void setDbPassword(String dbPassword) {
			this.dbPassword = dbPassword;
		}
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
		public void setDbPort(int dbPort) {
			this.dbPort = dbPort;
		}
		public String getHostAddress() {			
			return hostAddress;
		}
		public void setHostAddress(String hostAddress) {
			this.hostAddress = hostAddress;
		}

}
