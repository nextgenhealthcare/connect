package com.webreach.mirth.server.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.ibatis.common.logging.LogFactory;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.util.PropertyLoader;

public class SqlConfig {
    private static SqlMapClient sqlMapClient = null;

    private SqlConfig() {

    }

    /* This method loads the iBatis SQL config file for the database in use,
     * then appends sqlMap entries from any installed plugins, and returns
     * a SQLMapClient.
     */
    public static SqlMapClient getSqlMapClient() {
        synchronized (SqlConfig.class) {
            if (sqlMapClient == null) {
                try {
                    LogFactory.selectLog4JLogging();
                    Map<String, PluginMetaData> plugins = ControllerFactory.getFactory().createExtensionController().getPluginMetaData();
                    String database = PropertyLoader.getProperty(PropertyLoader.loadProperties("mirth"), "database");
                    BufferedReader br = new BufferedReader(Resources.getResourceAsReader(database + System.getProperty("file.separator") + database + "-SqlMapConfig.xml"));
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(br));
                    Element sqlMapConfigElement = document.getDocumentElement();

                    for (String pluginName : plugins.keySet()) {
                        if (plugins.get(pluginName).getSqlMapConfigs() != null) {
                            String sqlMapConfig = plugins.get(pluginName).getSqlMapConfigs().get(database);

                            if ((sqlMapConfig != null) && (sqlMapConfig.length() > 0)) {
                                Element sqlMapElement = document.createElement("sqlMap");
                                sqlMapElement.setAttribute("resource", sqlMapConfig);
                                sqlMapConfigElement.appendChild(sqlMapElement);
                            }
                        }
                    }

                    DocumentSerializer docSerializer = new DocumentSerializer();
                    Reader reader = new StringReader(docSerializer.toXML(document));

                    if (database.equalsIgnoreCase("derby")) {
                        Properties props = PropertyLoader.loadProperties(database + "-SqlMapConfig");
                        props.setProperty("mirthHomeDir", ControllerFactory.getFactory().createConfigurationController().getBaseDir());
                        sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader, props);
                    } else {
                        sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return sqlMapClient;
        }
    }
}