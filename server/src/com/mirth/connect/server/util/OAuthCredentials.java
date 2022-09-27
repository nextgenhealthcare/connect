package com.mirth.connect.server.util;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.util.HttpUtil;

public class OAuthCredentials {
	private static final int TIMEOUT = 10000;
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	private static Logger logger = LogManager.getLogger(OAuthCredentials.class);
	
	private String url;
	private String clientId;
	private String clientSecret;
	private OAuthToken oAuthToken;
	private AtomicBoolean tokenExpired;
	
	public OAuthCredentials(String url, String clientId, String clientSecret) {
		this.url = url;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		tokenExpired = new AtomicBoolean(false);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	public OAuthToken getOAuthToken() {
		if (oAuthToken == null || tokenExpired.get()) {
			synchronized (this) {
				if (oAuthToken == null || tokenExpired.get()) {
					oAuthToken = fetchBearerToken();
				}
			}
		}
		return oAuthToken;
	}
	
	public boolean isTokenExpired() {
		return tokenExpired.get();
	}
	
	public void setTokenExpired(boolean tokenExpired) {
		this.tokenExpired.set(tokenExpired);
	}
	
	protected OAuthToken fetchBearerToken() {
		OAuthToken newOAuthToken = null;
		
		try {
			URL urlObj = new URL(url);
	        HttpHost target = new HttpHost(urlObj.getHost(), 443, "https");
	        
	        CredentialsProvider credsProvider = new BasicCredentialsProvider();
	        AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
	        Credentials credentials = new UsernamePasswordCredentials(clientId, clientSecret);
	        credsProvider.setCredentials(authScope, credentials);
	        AuthCache authCache = new BasicAuthCache();
	        RegistryBuilder<AuthSchemeProvider> registryBuilder = RegistryBuilder.<AuthSchemeProvider> create();
	        registryBuilder.register(AuthSchemes.BASIC, new BasicSchemeFactory(CHARSET));
	        authCache.put(target, new BasicScheme());
	        
	        HttpClientContext context = HttpClientContext.create();
	        context.setCredentialsProvider(credsProvider);
	        context.setAuthSchemeRegistry(registryBuilder.build());
	        context.setAuthCache(authCache);
	        context.setRequestConfig(RequestConfig.custom().setConnectTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build());
	        
	        HttpClientBuilder clientBuilder = HttpClients.custom();
	        HttpUtil.configureClientBuilder(clientBuilder);
	        CloseableHttpClient client = clientBuilder.build();
	        
	        HttpRequestBase request = new HttpPost(urlObj.getPath());
	        List<NameValuePair> parameters = new ArrayList<>();
	        parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
	        ((HttpEntityEnclosingRequestBase) request).setEntity(
	        		EntityBuilder.create().
	        		setContentType(ContentType.APPLICATION_FORM_URLENCODED)
	        		.setParameters(parameters).build());
	        
	        CloseableHttpResponse httpResponse = null;
	        
	        try {
				httpResponse = client.execute(target, request, context);
				String responseContent = IOUtils.toString(httpResponse.getEntity().getContent(), CHARSET);
				newOAuthToken = new OAuthToken(responseContent);
				tokenExpired.set(false);
			} catch (Exception e) {
				logger.error("Error attempting to retrieve OAuth bearer token.", e);
			} finally {
				HttpUtil.closeVeryQuietly(httpResponse);
			}
		} catch (Exception e) {
			logger.error("Error attempting to retrieve OAuth bearer token." , e);
		}
        
        return newOAuthToken;
	}
	
	public static class OAuthToken {
		private String accessToken;
		private String tokenType;
		private long expiresIn;
		
		public OAuthToken(String accessToken, String tokenType, long expiresIn) {
			this.accessToken = accessToken;
			this.tokenType = tokenType;
			this.expiresIn = expiresIn;
		}
		
		public OAuthToken(String jsonResponse) {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());

	        try {
	            JsonNode json = mapper.readTree(jsonResponse);
	            accessToken = json.get("access_token").textValue();
	            tokenType = json.get("token_type").textValue();
	            expiresIn = json.get("expires_in").longValue();
	        } catch (Exception e) {
	            logger.error("Error constructing OAuthToken from JSON response.", e);
	        }
		}
		
		public String getAccessToken() {
			return accessToken;
		}

		public String getTokenType() {
			return tokenType;
		}

		public long getExpiresIn() {
			return expiresIn;
		}
	}
	
	@SuppressWarnings("serial")
	public static class ExpiredBearerTokenException extends Exception {
		public ExpiredBearerTokenException(String message) {
			super(message);
		}
	}
}
