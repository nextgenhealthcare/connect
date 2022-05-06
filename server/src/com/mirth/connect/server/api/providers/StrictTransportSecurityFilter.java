package com.mirth.connect.server.api.providers;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.PropertiesConfiguration;

public class StrictTransportSecurityFilter implements Filter {

    boolean strictTransportSecurityEnabled;

    public StrictTransportSecurityFilter(PropertiesConfiguration mirthProperties) {
        strictTransportSecurityEnabled = mirthProperties.getBoolean("http.stricttransportsecurity", true);

    }

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (strictTransportSecurityEnabled) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

}
