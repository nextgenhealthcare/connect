package com.mirth.connect.plugins.uima;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.plugins.uima.model.UimaPipeline;
import com.mirth.connect.util.PropertyLoader;

public class UimaService implements ServicePlugin {
    public static final String UIMA_SERVICE_PLUGINPOINT = "UIMA Service";
    public static final String METHOD_TEST_PIPELINE = "testPipelines";
    public static final String METHOD_GET_PIPELINES = "getPipelines";
    private static final Pattern jmxStandardHostPattern = Pattern.compile(".*:[0-9]+$");
    
    private Logger logger = Logger.getLogger(this.getClass());
    private Properties properties = null;

    @Override
    public Properties getDefaultProperties() {
        Properties defaultProperties = new Properties();
        return defaultProperties;
    }
 
    @Override
    public void init(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        if (METHOD_TEST_PIPELINE.equals(method)) {
            // this is just for testing the connection, and we don't save on this one!
            return testPipelines((Properties) object);
        } else if (METHOD_GET_PIPELINES.equals(method)) {
            return getPipelines();
        }

        return null;
    }

    @Override
    public void start() {
        
    }
    
    /**
     * This will create a connection using the properties saved in the plugin
     * @return
     * @throws IOException
     */
    private JMXConnector createConnection() throws IOException {
        // XXX: Do we want to make this a default?
        String jmxServerUrl = PropertyLoader.getProperty(this.properties, "jmxServerUrl", "127.0.0.1:1099");
        String jmxUsername = PropertyLoader.getProperty(this.properties, "jmxUsername");
        String jmxPassword = PropertyLoader.getProperty(this.properties, "jmxPassword");

        return createConnection(jmxServerUrl, jmxUsername, jmxPassword);
    }
    
    /**
     * This creates a connection using specific connection settings
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    private JMXConnector createConnection(String url, String username, String password) throws IOException {
        JMXServiceURL jmxUrl = null;

        Map<String, String[]> environment = new HashMap<String, String[]>();

        // setup the authentication
        if (username != null && !"".equals(username)) {
            environment.put(JMXConnector.CREDENTIALS, new String[] { username, password });
        }

        // setup the connection URL
        if (jmxStandardHostPattern.matcher(url).matches()) {
            jmxUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + url + "/jmxrmi");
        } else {
            // this is a full on URL (service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/jmxrmi) so just use the whole thing
            jmxUrl = new JMXServiceURL(url);
        }

        return JMXConnectorFactory.connect(jmxUrl, environment);
    }
    

    @Override
    public void stop() {
        
    }

    @Override
    public void update(Properties properties) {
        this.properties = properties;
    }

    @Override
	public ExtensionPermission[] getExtensionPermissions() {
        return null;
    }
	
    private List<UimaPipeline> testPipelines(Properties props) {
        JMXConnector conn = null;
        
        try {
            conn = createConnection(props.getProperty("jmx.url"), props.getProperty("jmx.username"), props.getProperty("jmx.password"));
            return getPipeLines(conn);
        } catch (Exception e) {
            logger.error("Unable to connect to the UIMA JMX service", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException e) {
                    
                }
            }
        }
    }
	
	/**
	 * Gets all pipelines using a pre-created connnection
	 * @param conn
	 * @return
	 * @throws IOException
	 * @throws MalformedObjectNameException
	 * @throws NullPointerException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
    private List<UimaPipeline> getPipeLines(JMXConnector conn) throws IOException, MalformedObjectNameException, NullPointerException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        MBeanServerConnection mxbc = conn.getMBeanServerConnection();

        List<UimaPipeline> pipelines = new ArrayList<UimaPipeline>();

        Set<ObjectName> brokers = mxbc.queryNames(new ObjectName("org.apache.activemq:BrokerName=*,Type=Broker"), null);

        for (ObjectName broker : brokers) {
            ObjectName[] queues = (ObjectName[]) mxbc.getAttribute(broker, "Queues");
            String openWireUrl = (String) mxbc.getAttribute(broker, "OpenWireURL");
            for (ObjectName objectName : queues) {
                UimaPipeline tempPipeline = new UimaPipeline();
                tempPipeline.setName((String) mxbc.getAttribute(objectName, "Name"));
                tempPipeline.setConsumerCount((Long) mxbc.getAttribute(objectName, "ConsumerCount"));
                tempPipeline.setMessageCount((Long) mxbc.getAttribute(objectName, "EnqueueCount"));
                tempPipeline.setPendingMessageCount((Long) mxbc.getAttribute(objectName, "InFlightCount"));
                tempPipeline.setDequeueCount((Long) mxbc.getAttribute(objectName, "DequeueCount"));
                tempPipeline.setMemoryPercentUsage((Integer) mxbc.getAttribute(objectName, "MemoryPercentUsage"));
                tempPipeline.setCursorPercentUsage((Integer) mxbc.getAttribute(objectName, "CursorPercentUsage"));
                tempPipeline.setAvgEnqueueTime((Double) mxbc.getAttribute(objectName, "AverageEnqueueTime"));
                tempPipeline.setJmsUrl(openWireUrl);
                pipelines.add(tempPipeline);
            }
        }

        return pipelines;
    }
	
	/**
	 * Returns all pipelines using the default connection info
	 * @return
	 * @throws IOException
	 * @throws MalformedObjectNameException
	 * @throws NullPointerException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
    private List<UimaPipeline> getPipelines() {
        JMXConnector conn = null;
        List<UimaPipeline> pipelines = new ArrayList<UimaPipeline>();
        
        try {
            conn = createConnection();
            pipelines = getPipeLines(conn);
        } catch (Exception e) {
            logger.warn("Unable to connect to the UIMA JMX service", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException e) {

                }    
            }
        }
        
        return pipelines;
    }

    @Override
    public String getPluginPointName() {
        return UIMA_SERVICE_PLUGINPOINT;
    }
}
