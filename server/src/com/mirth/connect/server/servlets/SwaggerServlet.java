/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.util.ArrayList;
import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Version;

import io.swagger.v3.jaxrs2.integration.ServletOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

public class SwaggerServlet extends HttpServlet {

	private String basePath;
	private Version version;
	private Version apiVersion;
	private Set<String> resourcePackages;
	private Set<Class<?>> resourceClasses;
	private boolean allowHTTP;
	private Logger logger = Logger.getLogger(this.getClass());

	public SwaggerServlet(String basePath, Version version, Version apiVersion, Set<String> resourcePackages,
			Set<Class<?>> resourceClasses, boolean allowHTTP) {
		this.basePath = basePath;
		this.version = version;
		this.apiVersion = apiVersion;
		this.resourcePackages = resourcePackages;
		this.resourceClasses = resourceClasses;
		this.allowHTTP = allowHTTP;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		OpenAPI oas = new OpenAPI();
		
		List<Server> servers = new ArrayList<Server>();
		servers.add(new Server().url(basePath));
		oas.servers(servers);
		
		Info info = new Info().title("NextGen Connect Client API")
				.description("Swagger documentation for the NextGen Connect Client API.")
				.version(apiVersion.toString());

		oas.info(info);
		SwaggerConfiguration oasConfig = new SwaggerConfiguration()
				.openAPI(oas)
				.resourceClasses(resourceClasses.stream().map(Class::getName).collect(Collectors.toSet()));

		try {
			new ServletOpenApiContextBuilder()
				.servletConfig(config)
				.openApiConfiguration(oasConfig)
				.buildContext(true)
				.read();			
		} catch (OpenApiConfigurationException e) {
			logger.error("Failed to initialize Swagger servlet", e);
			throw new ServletException(e.getMessage(), e);
		}
	}
}