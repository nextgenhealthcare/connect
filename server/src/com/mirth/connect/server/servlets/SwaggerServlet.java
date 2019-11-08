/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.SwaggerDefinition;
//import io.swagger.jaxrs.config.BeanConfig;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.mirth.connect.client.core.Version;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

public class SwaggerServlet extends HttpServlet {

	private String basePath;
	private Version version;
	private Version apiVersion;
	private Set<String> resourcePackages;
	private Set<Class<?>> resourceClasses;
	private boolean allowHTTP;

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

		OpenAPI oas = new OpenAPI();
		Info info = new Info().title("Swagger Sample App - independent config exposed by dedicated servlet")
				.description("This is a sample server Petstore server.  You can find out more about Swagger "
						+ "at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, "
						+ "you can use the api key `special-key` to test the authorization filters.")
				.termsOfService("http://swagger.io/terms/").contact(new Contact().email("apiteam@swagger.io"))
				.license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"));

		oas.info(info);
		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas)
				.resourcePackages(Stream.of("io.swagger.sample.resource").collect(Collectors.toSet()));

		try {
			new JaxrsOpenApiContextBuilder().servletConfig(config).openApiConfiguration(oasConfig).buildContext(true);
		} catch (OpenApiConfigurationException e) {
			throw new ServletException(e.getMessage(), e);
		}

	}
}