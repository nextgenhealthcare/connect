package com.webreach.mirth.server.controllers;

import java.io.Reader;

import com.ibatis.common.logging.LogFactory;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class SqlConfig {
	private static final SqlMapClient sqlMap;

	static {
		try {
			String resource = "SqlMapConfig.xml";
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