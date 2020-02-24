package com.cloudsolutions.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SSLUtils {

	SSLContext atnaContext = null;

	public static KeyStore readKeyStoreFile(String filePath, String password) {
		KeyStore keyStore = null;
		try {
			FileInputStream is = new FileInputStream(new File(filePath));
			keyStore = KeyStore.getInstance("JCEKS");
			keyStore.load(is, password.toCharArray());
			IOUtils.closeQuietly(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyStore;
	}

//	//Hacky Way just for ref
//	public static void switchToProjectSSL_Context() {
//		PropertiesConfiguration mirthProperties;
//		try {
//			mirthProperties = new PropertiesConfiguration("mirth.properties");
//			String atnakeystorePath = mirthProperties.getProperty("atna.keystore.path").toString();
//			String atnakeystorePass = mirthProperties.getProperty("atna.keystore.storepass").toString();
//			KeyStore keyStore = SSLUtils.readKeyStoreFile(atnakeystorePath, atnakeystorePass);
//			System.setProperty("javax.net.ssl.keyStore", atnakeystorePath);
//			System.setProperty("javax.net.ssl.keyStorePassword", atnakeystorePass);
//			System.setProperty("javax.net.ssl.trustStore", atnakeystorePath);
//			System.setProperty("javax.net.ssl.trustStorePassword", atnakeystorePass);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//		}
//	}

	public static void switchToProjectSSL_Context(HttpClientBuilder clientBuilder, int timeout) {
		try {
			// Setting SSL_Socket_Factory to HttpClientBuilder
			SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(SSLUtils.getATNAContext(),
					null, null, new NoopHostnameVerifier());
			clientBuilder.setSSLSocketFactory(sslConnectionFactory);
			// Creating registry for factory
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("https", sslConnectionFactory).build();
			// Setting new connection manager
			BasicHttpClientConnectionManager httpsClientConnectionManager = new BasicHttpClientConnectionManager(
					registry);

			httpsClientConnectionManager.setSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build());
			clientBuilder.setConnectionManager(httpsClientConnectionManager);
		} catch (Exception e) {
			System.err.println("Error occured while switching to project SSLs. For details check below trace..");
			e.printStackTrace();
		}
	}

	public static SSLContext getATNAContext() throws KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, ConfigurationException {
		PropertiesConfiguration mirthProperties = new PropertiesConfiguration("mirth.properties");
		String atnakeystorePath = mirthProperties.getProperty("atna.keystore.path").toString();
		String atnakeystorePass = mirthProperties.getProperty("atna.keystore.storepass").toString();
		KeyStore keyStore = SSLUtils.readKeyStoreFile(atnakeystorePath, atnakeystorePass);
		return provideSSLContext(keyStore, atnakeystorePass.toCharArray());
	}

	@Provides
	@Singleton
	static SSLContext provideSSLContext(KeyStore keystore, char[] password)
			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
		X509KeyManager customKeyManager = getKeyManager("SunX509", keystore, password);
		X509KeyManager jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);
		X509TrustManager customTrustManager = getTrustManager("SunX509", keystore);
		X509TrustManager jvmTrustManager = getTrustManager(defaultAlgorithm, null);

		KeyManager[] keyManagers = { new CompositeX509KeyManager(ImmutableList.of(jvmKeyManager, customKeyManager)) };
		TrustManager[] trustManagers = {
				new CompositeX509TrustManager(ImmutableList.of(jvmTrustManager, customTrustManager)) };

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		SSLParameters params= SSLContext.getDefault().getSupportedSSLParameters();
		params.setProtocols(new String[] {"SSLv2Hello", "SSLv3", "TLSv1.1", "TLSv1.2"}); //disables TLSv1
		
		sslContext.init(keyManagers, trustManagers, null);
		return sslContext;
	}

	private static X509KeyManager getKeyManager(String algorithm, KeyStore keystore, char[] password)
			throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
		KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
		factory.init(keystore, password);
		return Iterables.getFirst(Iterables.filter(Arrays.asList(factory.getKeyManagers()), X509KeyManager.class),
				null);
	}

	private static X509TrustManager getTrustManager(String algorithm, KeyStore keystore)
			throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
		factory.init(keystore);
		return Iterables.getFirst(Iterables.filter(Arrays.asList(factory.getTrustManagers()), X509TrustManager.class),
				null);
	}

}
