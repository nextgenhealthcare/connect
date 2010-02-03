/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.core.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p/>
 * TrustSSLProtocolSocketFactory allows you exercise full control over the
 * HTTPS server certificates you are going to trust.  Instead of relying
 * on the Certificate Authorities already present in "jre/lib/security/cacerts",
 * TrustSSLProtocolSocketFactory only trusts the public certificates you provide
 * to its constructor.
 * </p>
 * <p/>
 * TrustSSLProtocolSocketFactory can be used to create SSL {@link Socket}s
 * that accepts self-signed certificates.  Unlike EasySSLProtocolSocketFactory,
 * TrustSSLProtocolSocketFactory can be used in production.  This is because
 * it forces you to pre-install the self-signed certificate you are going to
 * trust locally.
 * <p/>
 * TrustSSLProtocolSocketFactory can parse both Java Keystore Files (*.jks)
 * and base64 PEM encoded public certificates (*.pem).
 * </p>
 * <p/>
 * <p/>
 * <p/>
 * Example of using TrustSSLProtocolSocketFactory
 * <pre>
 * 1.  First we must find the certificate we want to trust.  In this example
 *     we'll use gmail.google.com's certificate.
 * <p/>
 *   openssl s_client -showcerts -connect gmail.google.com:443
 * <p/>
 * 2.  Cut & paste into a "cert.pem" any certificates you are interested in
 *     trusting in accordance with your security policies.  In this example I'll
 *     actually use the current "gmail.google.com" certificate (instead of the
 *     Thawte CA certificate that signed the gmail certificate - that would be
 *     too boring) - but it expires on June 7th, 2006, so this example won't be
 *     useful for very long!
 * <p/>
 * Here's what my "cert.pem" file looks like:
 * <p/>
 * -----BEGIN CERTIFICATE-----
 * MIIDFjCCAn+gAwIBAgIDP3PeMA0GCSqGSIb3DQEBBAUAMEwxCzAJBgNVBAYTAlpB
 * MSUwIwYDVQQKExxUaGF3dGUgQ29uc3VsdGluZyAoUHR5KSBMdGQuMRYwFAYDVQQD
 * Ew1UaGF3dGUgU0dDIENBMB4XDTA1MDYwNzIyMTI1N1oXDTA2MDYwNzIyMTI1N1ow
 * ajELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDU1v
 * dW50YWluIFZpZXcxEzARBgNVBAoTCkdvb2dsZSBJbmMxGTAXBgNVBAMTEGdtYWls
 * Lmdvb2dsZS5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALoRiWYW0hZw
 * 9TSn3s9912syZg1CP2TaC86PU1Ao2qf3pVu7Mx10Wl8W+aKZrQlvrYjTwku4sEh+
 * 9uI+gWnfmCd0OyVcXr1eFOGCYiiyaPv79Wtb0m0d8GuiRSJhYkZGzGlgFViws2vR
 * BAMCD2fdp7WGJUVGYOO+s52dgAMUHQXxAgMBAAGjgecwgeQwKAYDVR0lBCEwHwYI
 * KwYBBQUHAwEGCCsGAQUFBwMCBglghkgBhvhCBAEwNgYDVR0fBC8wLTAroCmgJ4Yl
 * aHR0cDovL2NybC50aGF3dGUuY29tL1RoYXd0ZVNHQ0NBLmNybDByBggrBgEFBQcB
 * AQRmMGQwIgYIKwYBBQUHMAGGFmh0dHA6Ly9vY3NwLnRoYXd0ZS5jb20wPgYIKwYB
 * BQUHMAKGMmh0dHA6Ly93d3cudGhhd3RlLmNvbS9yZXBvc2l0b3J5L1RoYXd0ZV9T
 * R0NfQ0EuY3J0MAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQEEBQADgYEAktM1l1cV
 * ebi+Uo6fCE/eLnvvY6QbNNCsU5Pi9B5E1BlEUG+AGpgzE2cSPw1N4ZZb+2AWWwjx
 * H8/IrJ143KZZXM49ri3Z2e491Jj8qitrMauT7/hb16Jw6I02/74/do4TtHu/Eifr
 * EZCaSOobSHGeufHjlqlC3ehC4Bx4mLexIMk=
 * -----END CERTIFICATE-----
 * <p/>
 * 3.  Run "openssl x509" to analyze the certificate more deeply.  This helps
 *     us answer questions like "Do we really want to trust it?  When does it
 *     expire? What's the value of the CN (Common Name) field?".
 * <p/>
 *     "openssl x509" is also super cool, and will impress all your friends,
 *     coworkers, family, and that cute girl at the starbucks.   :-)
 * <p/>
 *     If you dig through "man x509" you'll find this example.  Run it:
 * <p/>
 *    openssl x509 -in cert.pem -noout -text
 * <p/>
 * 4.  Rename "cert.pem" to "gmail.pem" so that step 5 works.
 * <p/>
 * 5.  Setup the TrustSSLProtocolSocketFactory to trust "gmail.google.com"
 *     for URLS of the form "https-gmail://" - but don't trust anything else
 *     when using "https-gmail://":
 * <p/>
 *     TrustSSLProtocolSocketFactory sf = new TrustSSLProtocolSocketFactory( "/path/to/gmail.pem" );
 *     Protocol trustHttps = new Protocol("https-gmail", sf, 443);
 *     Protocol.registerProtocol("https-gmail", trustHttps);
 * <p/>
 *     HttpClient client = new HttpClient();
 *     GetMethod httpget = new GetMethod("https-gmail://gmail.google.com/");
 *     client.executeMethod(httpget);
 * <p/>
 * 6.  Notice that "https-gmail://" cannot connect to "www.wellsfargo.com" -
 *     the server's certificate isn't trusted!  It would still work using
 *     regular "https://" because Java would use the "jre/lib/security/cacerts"
 *     file.
 * <p/>
 *     httpget = new GetMethod("https-gmail://www.wellsfargo.com/");
 *     client.executeMethod(httpget);
 * <p/>
 * javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: No trusted certificate found
 * <p/>
 * <p/>
 * 7.  Of course "https-gmail://" cannot connect to hosts where the CN field
 *     in the certificate doesn't match the hostname.  The same is supposed to
 *     be true of regular "https://", but HTTPClient is a bit lenient.
 * <p/>
 *     httpget = new GetMethod("https-gmail://gmail.com/");
 *     client.executeMethod(httpget);
 * <p/>
 * javax.net.ssl.SSLException: hostname in certificate didn't match: &lt;gmail.com> != &lt;gmail.google.com>
 * <p/>
 * <p/>
 * 8.  You can use "*.jks" files instead of "*.pem" if you prefer.  Use the 2nd constructor
 *     in that case to pass along the JKS password:
 * <p/>
 *   new TrustSSLProtocolSocketFactory( "/path/to/gmail.jks", "my_password".toCharArray() );
 * <p/>
 * </pre>
 *
 * @author <a href="http://juliusdavies.ca/">Julius Davies</a>
 *         <p/>
 *         <p/>
 *         DISCLAIMER: HttpClient developers DO NOT actively support this component.
 *         The component is provided as a reference material, which may be inappropriate
 *         for use without additional customization.
 *         </p>
 * @since 17-Feb-2006
 */

public class TrustSSLProtocolSocketFactory implements SecureProtocolSocketFactory
{

	/**
	 * Log object for this class.
	 */
	private static final Log LOG = LogFactory.getLog( EasySSLProtocolSocketFactory.class );

	private SSLContext sslcontext = null;

	/**
	 * @param pathToTrustStore Path to either a ".jks" Java Key Store, or a
	 *                         ".pem" base64 encoded certificate.  If it's a
	 *                         ".pem" base64 certificate, the file must start
	 *                         with "------BEGIN CERTIFICATE-----", and must end
	 *                         with "-------END CERTIFICATE--------".
	 */
	public TrustSSLProtocolSocketFactory( String pathToTrustStore )
	      throws Exception
	{
		this( pathToTrustStore, null );
	}

	/**
	 * @param pathToTrustStore Path to either a ".jks" Java Key Store, or a
	 *                         ".pem" base64 encoded certificate.  If it's a
	 *                         ".pem" base64 certificate, the file must start
	 *                         with "------BEGIN CERTIFICATE-----", and must end
	 *                         with "-------END CERTIFICATE--------".
	 * @param password         Password to open the ".jks" file.  If "truststore"
	 *                         is a ".pem" file, then password can be null; if
	 *                         password isn't null and we're using a ".pem" file,
	 *                         then technically, this becomes the password to
	 *                         open up the special in-memory keystore we create
	 *                         to hold the ".pem" file, but it's not important at
	 *                         all.
	 *
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public TrustSSLProtocolSocketFactory( String pathToTrustStore, char[] password )
	      throws CertificateException, KeyStoreException, IOException,
	             NoSuchAlgorithmException, KeyManagementException
	{
		Collection certs = Collections.EMPTY_LIST;
		KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
		CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
		InputStream in = null;

		// Instead of relying on file suffixes (e.g. *.jks), we figure out what
		// type of file we've got by analyzing the first 64 bytes.  If we find
		// the string "------BEGIN CERTIFICATE-----", then it's a PEM file.
		// Otherwise, it's a JKS file.
		byte[] firstFewChars = new byte[ 64 ];
		try
		{
			in = new FileInputStream( pathToTrustStore );
			int c = in.read();
			int i = 0;
			while ( c != -1 && i < firstFewChars.length )
			{
				firstFewChars[ i++ ] = (byte) c;
				c = in.read();
			}
		}
		finally
		{
			if ( in != null )
			{
				in.close();
			}
		}

		// Okay, read first 64 bytes.  Let's start over from the beginning again.
		in = new FileInputStream( pathToTrustStore );
		try
		{
			String s = new String( firstFewChars );
			s = s.trim().toUpperCase(); // might as well be case-insensitive
			int x = s.indexOf( "BEGIN CERTIFICATE" );
			if ( x >= 0 )
			{
				// Since the word "BEGIN CERTIFICATE" was found, there's a good
				// chance our CertificateFactory can deal with this file.
				certs = cf.generateCertificates( in );
			}
			boolean isPEM = !certs.isEmpty();
			if ( isPEM )
			{
				// It's definitely a PEM file!  It has "-----BEGIN CERTIFICATE----"
				// in it, and cf.generateCertificate() really worked!

				// small gotch'ya:  KeyStores aren't valid until load() has been
				// called on them.  So we just call load( null, null ).  Definitely
				// feels somehow wrong.  (ps.  usually password is null)
				ks.load( null, password );
				Iterator it = certs.iterator();
				int count = 0;
				while ( it.hasNext() )
				{
					X509Certificate cert = (X509Certificate) it.next();

					// I could be fancy and parse out the CN field from the
					// certificate's subject, but these names don't actually matter
					// at all - I think they just have to be unique.
					ks.setCertificateEntry( "httpclient-trust-cert-" + count, cert );
					count++;
				}
			}
			else
			{
				// It's probably a JKS file.  If not, this will throw a nice
				// exception.
				ks.load( in, password );
			}
		}
		finally
		{
			if ( in != null )
			{
				in.close();
			}
		}

		this.sslcontext = SSLContext.getInstance( "SSL" );
		String algorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( algorithm );
		tmf.init( ks );
		sslcontext.init( null, tmf.getTrustManagers(), null );
	}

	public final static void verify( String host, Socket socket )
	      throws IOException
	{
		boolean isSecure = socket instanceof SSLSocket;
		if ( !isSecure )
		{
			// Don't bother verifying if it's not a secure socket.
			return;
		}

		SSLSocket s = (SSLSocket) socket;
		SSLSession session = s.getSession();
		Certificate[] certs = null;
		try
		{
			certs = session.getPeerCertificates();
		}
		catch ( SSLPeerUnverifiedException spue )
		{
			// let's see if this unearths the real problem:
			s.startHandshake();
			throw spue;
		}
		X509Certificate cert = (X509Certificate) certs[ 0 ];

		/*
		// toString() seems to do a better job than getName() on some
		// of the complicated conversions with X500 - at least in SUN's
		// Java 1.4.2_09.
		//
		// For example, getName() gives me this:
		// 1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d
		//
		// whereas toString() gives me this:
		// EMAILADDRESS=juliusdavies@cucbc.com
		*/

		String name = cert.getSubjectX500Principal().toString();
		int x = name.indexOf( "CN=" );
		int y = name.indexOf( ',', x );
		y = y >= 0 ? y : name.length();

		/*
		// X500 CommonName parsing is actually much, much harder than this -
		// there are all sorts of special escape characters and hexadecimal
		// conversions to consider (see: <code>RFC 2253</code>).  Maybe toString()
		// is doing these already?  I don't know.
		//
		// (Thanks to Sebastian Hauer's StrictSSLProtocolSocketFactory for
		// pointing out how tricky X500 parsing can be!)
		*/
		
		name = name.substring( x + 3, y );
		if ( !host.equals( name ) )
		{
			throw new SSLException( "hostname in certificate didn't match: <" + host + "> != <" + name + ">" );
		}
	}

	public Socket createSocket( final String host, final int port )
	      throws IOException, UnknownHostException
	{

		return createSocket( host, port, null, 0 );

	}

	public Socket createSocket( String host,
	                            int port,
	                            InetAddress localHost,
	                            int localPort )
	      throws IOException, UnknownHostException
	{

		return createSocket( host, port, localHost, localPort, null );
	}

	/**
	 * Attempts to get a new socket connection to the given host within the
	 * given time limit.
	 *
	 * @param host      the host name/IP
	 * @param port      the port on the host
	 * @param localHost the local host name/IP to bind the socket to
	 * @param localPort the port on the local machine
	 * @param params    {@link HttpConnectionParams Http connection parameters}
	 * @return Socket a new socket
	 * @throws IOException          if an I/O error occurs while creating thesocket
	 * @throws UnknownHostException if the IP address of the host cannot be
	 *                              determined
	 */
	public Socket createSocket( final String host,
	                            final int port,
	                            final InetAddress localHost,
	                            final int localPort,
	                            final HttpConnectionParams params )
	      throws IOException, UnknownHostException
	{

		Socket s = sslcontext.getSocketFactory().createSocket();
		InetSocketAddress dest = new InetSocketAddress( host, port );
		InetSocketAddress src = new InetSocketAddress( localHost, localPort );
		int timeout = params != null ? params.getConnectionTimeout() : 0;

		s.bind( src );
		s.connect( dest, timeout );
		verify( host, s );
		return s;
	}

	public Socket createSocket( final Socket socket,
	                            final String host,
	                            final int port,
	                            final boolean autoClose )
	      throws IOException, UnknownHostException
	{

		Socket s = sslcontext.getSocketFactory().createSocket( socket,
		                                                       host,
		                                                       port,
		                                                       autoClose );

		verify( host, s );
		return s;
	}


	public boolean equals( Object obj )
	{
		return ( ( obj != null ) && obj.getClass().equals( getClass() ) );
	}

	public int hashCode()
	{
		return getClass().hashCode();
	}

}
