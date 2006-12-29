package com.webreach.mirth.server.util;

import java.io.Reader;
import java.util.Properties;

import com.ibatis.common.logging.LogFactory;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.webreach.mirth.util.PropertyLoader;

public class SqlConfig {
	private static Properties mirthProperties = PropertyLoader.loadProperties("mirth");
	private static final SqlMapClient sqlMap;

	static {
		try {
			String database = mirthProperties.getProperty("database");
			String resource = database + "-SqlMapConfig.xml";
			LogFactory.selectLog4JLogging();
			Reader reader = Resources.getResourceAsReader(resource);
			sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static SqlMapClient getSqlMapInstance() {
		return sqlMap;
	}
}