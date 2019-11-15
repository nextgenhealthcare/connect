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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.SwaggerDefinition;
//import io.swagger.jaxrs.config.BeanConfig;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Version;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.ServletOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.XmlWebOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.XML;
import io.swagger.v3.oas.models.security.SecurityRequirement;
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

	// @Override
	// public void init(ServletConfig config) throws ServletException {
	// super.init(config);
	//
	// BeanConfig swaggerConfig = new BeanConfig() {
	// @Override
	// public Set<Class<?>> classes() {
	// ConfigurationBuilder config = new ConfigurationBuilder();
	// config.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new
	// SubTypesScanner());
	//
	// if (CollectionUtils.isNotEmpty(resourcePackages)) {
	// for (String packageName : resourcePackages) {
	// config.addUrls(ClasspathHelper.forPackage(packageName));
	// }
	// config.setInputsFilter(new PackagePredicate(resourcePackages.toArray(new
	// String[resourcePackages.size()])));
	// }
	//
	// final Reflections reflections = new Reflections(config);
	// Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Api.class);
	// classes.addAll(reflections.getTypesAnnotatedWith(javax.ws.rs.Path.class));
	// classes.addAll(reflections.getTypesAnnotatedWith(SwaggerDefinition.class));
	// classes.addAll(resourceClasses);
	// return classes;
	// }
	// };
	//
	// swaggerConfig.setVersion(apiVersion.toString());
	// if (allowHTTP) {
	// swaggerConfig.setSchemes(new String[] { "http", "https" });
	// } else {
	// swaggerConfig.setSchemes(new String[] { "https" });
	// }
	// swaggerConfig.setBasePath(basePath);
	// swaggerConfig.setScan(true);
	// ScannerFactory.setScanner(version, swaggerConfig);
	// }
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
					
		// Create schema to represent java.util.Properties
//		Map<String, Schema<String>> properties = new HashMap<>();
//		properties.put("property", new Schema()
//				.type("string")
//				.xml(new XML()
//						.name("property")));
//		
//		OpenAPI oas = new OpenAPI()
//				.schema("properties", new Schema()
//						.title("Properties")
//						.properties(properties)
//						.xml(new XML()
//								.name("properties")));

		OpenAPI oas = new OpenAPI();
		
//		Server server = new Server();
//		server.url(basePath);
//		List<Server> servers = new ArrayList<Server>();
//		servers.add(server);
//		oas.servers(servers);
		
		Info info = new Info().title("NextGen Connect Client API")
				.description("Swagger documentation for the NextGen Connect Client API.")
//				.termsOfService("http://swagger.io/terms/").contact(new Contact().email("apiteam@swagger.io"))
				.license(new License().name("Mozilla Public License 1.1"))
//						.url("http://www.apache.org/licenses/LICENSE-2.0.html"))
				.version(apiVersion.toString());

		oas.info(info);
		SwaggerConfiguration oasConfig = new SwaggerConfiguration()
				.openAPI(oas)
				.prettyPrint(true)
//				.resourcePackages(resourcePackages);
				.resourceClasses(resourceClasses.stream().map(Class::getName).collect(Collectors.toSet()));
//				.readAllResources(true);

		try {
//			new JaxrsOpenApiContextBuilder()
//				.servletConfig(config)
//				.openApiConfiguration(oasConfig)
//				.buildContext(true);
//				.read();

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