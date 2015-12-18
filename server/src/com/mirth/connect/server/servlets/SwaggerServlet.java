/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.config.BeanConfig;

import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.server.api.ScannerFactory;
import com.mirth.connect.server.util.PackagePredicate;

public class SwaggerServlet extends HttpServlet {

    private String basePath;
    private Version version;
    private Version apiVersion;
    private Set<String> resourcePackages;
    private Set<Class<?>> resourceClasses;
    private boolean allowHTTP;

    public SwaggerServlet(String basePath, Version version, Version apiVersion, Set<String> resourcePackages, Set<Class<?>> resourceClasses, boolean allowHTTP) {
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

        BeanConfig swaggerConfig = new BeanConfig() {
            @Override
            public Set<Class<?>> classes() {
                ConfigurationBuilder config = new ConfigurationBuilder();
                config.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());

                if (CollectionUtils.isNotEmpty(resourcePackages)) {
                    for (String packageName : resourcePackages) {
                        config.addUrls(ClasspathHelper.forPackage(packageName));
                    }
                    config.setInputsFilter(new PackagePredicate(resourcePackages.toArray(new String[resourcePackages.size()])));
                }

                final Reflections reflections = new Reflections(config);
                Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Api.class);
                classes.addAll(reflections.getTypesAnnotatedWith(javax.ws.rs.Path.class));
                classes.addAll(reflections.getTypesAnnotatedWith(SwaggerDefinition.class));
                classes.addAll(resourceClasses);
                return classes;
            }
        };

        swaggerConfig.setVersion(apiVersion.toString());
        if (allowHTTP) {
            swaggerConfig.setSchemes(new String[] { "http", "https" });
        } else {
            swaggerConfig.setSchemes(new String[] { "https" });
        }
        swaggerConfig.setBasePath(basePath);
        swaggerConfig.setScan(true);
        ScannerFactory.setScanner(version, swaggerConfig);
    }
}