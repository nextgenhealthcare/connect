/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.digest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.TypeUtil;

import com.mirth.connect.plugins.httpauth.AuthenticationResult;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.RequestInfo;
import com.mirth.connect.plugins.httpauth.digest.DigestHttpAuthProperties.Algorithm;
import com.mirth.connect.plugins.httpauth.digest.DigestHttpAuthProperties.QOPMode;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class DigestAuthenticator extends Authenticator {

    private static final String USERNAME = "username";
    private static final String REALM = "realm";
    private static final String DOMAIN = "domain";
    private static final String URI = "uri";
    private static final String NONCE = "nonce";
    private static final String NONCE_COUNT = "nc";
    private static final String CLIENT_NONCE = "cnonce";
    private static final String ALGORITHM = "algorithm";
    private static final String QOP = "qop";
    private static final String RESPONSE = "response";
    private static final String OPAQUE = "opaque";
    private static final String STALE = "stale";

    private static final long MAX_NONCE_AGE = 60L * 1000L * 1000L * 1000L;
    private static final int MAX_NONCE_COUNT = 1024;

    private Logger logger = Logger.getLogger(getClass());
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private DigestAuthenticatorProvider provider;
    private SecureRandom rng = new SecureRandom();
    private Map<String, Nonce> nonceMap = new ConcurrentHashMap<String, Nonce>();

    public DigestAuthenticator(DigestAuthenticatorProvider provider) {
        this.provider = provider;
    }

    @Override
    public AuthenticationResult authenticate(RequestInfo request) {
        DigestHttpAuthProperties properties = getReplacedProperties(request);
        List<String> authHeaderList = request.getHeaders().get(HttpHeader.AUTHORIZATION.asString());
        Map<String, String> directives = new CaseInsensitiveMap<String, String>();
        String nonceString = null;
        String nonceCountString = null;
        String nonceOpaque = "";

        /*
         * This status is used to determine whether or not to send back a challenge. It's also used
         * to determine whether the nonce used by the client is stale (expired past the max nonce
         * age, or the max nonce count).
         */
        Status status = Status.INVALID;

        if (CollectionUtils.isNotEmpty(authHeaderList)) {
            String authHeader = authHeaderList.iterator().next();

            /*
             * This splits up the Authorization header into name-value pairs and puts them into the
             * directives map.
             */
            QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(authHeader, "=, ", true, false);
            String directive = null;
            String lastToken = null;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                char c;
                if (token.length() == 1 && ((c = token.charAt(0)) == '=' || c == ',' || c == ' ')) {
                    if (c == '=') {
                        directive = lastToken;
                    } else if (c == ',') {
                        directive = null;
                    }
                } else {
                    if (directive != null) {
                        directives.put(directive, token);
                        directive = null;
                    }
                    lastToken = token;
                }
            }

            nonceString = directives.get(NONCE);
            nonceCountString = directives.get(NONCE_COUNT);

            // The authentication attempt isn't valid without a nonce
            if (StringUtils.isNotBlank(nonceString)) {
                Nonce nonce = nonceMap.get(nonceString);

                if (nonce != null) {
                    // Nonce was found
                    nonceOpaque = nonce.getOpaque();

                    if (nonce.isExpired()) {
                        status = Status.STALE;
                    } else if (StringUtils.isNotBlank(nonceCountString)) {
                        /*
                         * Nonce count is supplied, so update the nonce with it. If the count is
                         * less than or equal to the current counter, it's an invalid request. If
                         * the count is greater than the max nonce count, the nonce is stale.
                         */
                        try {
                            status = nonce.updateCount(Long.parseLong(nonceCountString, 16));
                        } catch (NumberFormatException e) {
                            // If an exception occurs parsing the nonce count, leave the status as invalid
                        }
                    } else {
                        /*
                         * If no nonce count was supplied, just increment the internal counter by 1.
                         * If the count is greater than the max nonce count, the nonce is stale.
                         */
                        status = nonce.incrementCount();
                    }
                } else {
                    // Nonce has expired or never existed
                    status = Status.STALE;
                }
            }
        }

        // Remove expired nonces from the cache
        cleanupNonces();

        /*
         * If the status is valid or stale, attempt to calculate the digest and compare it to the
         * response hash. If the response hash is incorrect, the status should always be set to
         * invalid. If the response hash is correct but the nonce is stale, the stale directive
         * should be set to true in the response challenge.
         */
        if (status != Status.INVALID) {
            try {
                // Retrieve directives from the map
                String username = directives.get(USERNAME);
                String realm = directives.get(REALM);
                String uri = directives.get(URI);
                String response = directives.get(RESPONSE);
                String clientNonceString = directives.get(CLIENT_NONCE);
                String qop = directives.get(QOP);
                String algorithm = directives.get(ALGORITHM);
                String opaque = StringUtils.trimToEmpty(directives.get(OPAQUE));

                // Do some initial validation on required directives 
                if (StringUtils.isBlank(username)) {
                    throw new Exception("Username missing.");
                } else if (StringUtils.isBlank(realm)) {
                    throw new Exception("Realm missing.");
                } else if (uri == null) {
                    throw new Exception("URI missing.");
                } else if (StringUtils.isBlank(response)) {
                    throw new Exception("Response digest missing.");
                }

                String requestURI = request.getRequestURI();
                // Allow empty URI to match "/"
                if (StringUtils.isEmpty(uri) && StringUtils.equals(requestURI, "/")) {
                    requestURI = "";
                }

                if (!StringUtils.equalsIgnoreCase(properties.getRealm(), realm)) {
                    throw new Exception("Realm \"" + realm + "\" does not match expected realm \"" + properties.getRealm() + "\".");
                } else if (!StringUtils.equalsIgnoreCase(requestURI, uri)) {
                    throw new Exception("URI \"" + uri + "\" does not match the request URI \"" + requestURI + "\".");
                } else if (!StringUtils.equals(opaque, nonceOpaque)) {
                    throw new Exception("Opaque value \"" + opaque + "\" does not match the expected value \"" + properties.getOpaque() + "\".");
                }

                String password = properties.getCredentials().get(username);
                if (password == null) {
                    throw new Exception("Credentials for username " + username + " not found.");
                }

                /*
                 * Calculate H(A1).
                 * 
                 * Algorithm MD5: A1 = username:realm:password
                 * 
                 * Algorithm MD5-sess: A1 = H(username:realm:password):nonce:cnonce
                 */
                String ha1;

                if (algorithm == null || (StringUtils.equalsIgnoreCase(algorithm, Algorithm.MD5.toString()) && properties.getAlgorithms().contains(Algorithm.MD5))) {
                    ha1 = digest(username, realm, password);
                } else if (StringUtils.equalsIgnoreCase(algorithm, Algorithm.MD5_SESS.toString()) && properties.getAlgorithms().contains(Algorithm.MD5_SESS)) {
                    if (StringUtils.isBlank(clientNonceString)) {
                        throw new Exception("Client nonce missing.");
                    }
                    String credentialsDigest = digest(username, realm, password);
                    ha1 = digest(credentialsDigest, nonceString, clientNonceString);
                } else {
                    throw new Exception("Algorithm \"" + algorithm + "\" not supported.");
                }

                /*
                 * Calculate H(A2).
                 * 
                 * QOP undefined/auth: A2 = method:uri
                 * 
                 * QOP auth-int: A2 = method:uri:H(entityBody)
                 */
                String ha2;

                if (qop == null || (StringUtils.equalsIgnoreCase(qop, QOPMode.AUTH.toString()) && properties.getQopModes().contains(QOPMode.AUTH))) {
                    ha2 = digest(request.getMethod(), uri);
                } else if (StringUtils.equalsIgnoreCase(qop, QOPMode.AUTH_INT.toString()) && properties.getQopModes().contains(QOPMode.AUTH_INT)) {
                    String entityDigest = digest(request.getEntityProvider().getEntity());
                    ha2 = digest(request.getMethod(), uri, entityDigest);
                } else {
                    throw new Exception("Quality of protection mode \"" + qop + "\" not supported.");
                }

                /*
                 * Calculate response.
                 * 
                 * QOP undefined: response = H(H(A1):nonce:H(A2))
                 * 
                 * QOP auth/auth-int: response = H(H(A1):nonce:nc:cnonce:qop:H(A2))
                 */
                String rsp;

                if (qop == null) {
                    rsp = digest(ha1, nonceString, ha2);
                } else {
                    if (StringUtils.isBlank(nonceCountString)) {
                        throw new Exception("Nonce count missing.");
                    } else if (StringUtils.isBlank(clientNonceString)) {
                        throw new Exception("Client nonce missing.");
                    }
                    rsp = digest(ha1, nonceString, nonceCountString, clientNonceString, qop, ha2);
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("H(A1): " + ha1);
                    logger.trace("H(A2): " + ha2);
                    logger.trace("response: " + rsp);
                }

                if (StringUtils.equalsIgnoreCase(rsp, response)) {
                    /*
                     * If the status is valid, return a successful result. Otherwise, the status
                     * will remain stale and a challenge will be reissued.
                     */
                    if (status == Status.VALID) {
                        return AuthenticationResult.Success(username, realm);
                    }
                } else {
                    throw new Exception("Response digest \"" + response + "\" does not match expected digest \"" + rsp + "\".");
                }
            } catch (Exception e) {
                logger.debug("Error validating digest response.", e);
                status = Status.INVALID;
            }
        }

        /*
         * If we got to this point, an authentication challenge will be sent back, with a new nonce.
         * If the status is stale, the stale directive will also be set to true.
         */
        Nonce nonce = new Nonce(properties.getOpaque());
        nonceMap.put(nonce.getValue(), nonce);

        String contextPath = "/";
        try {
            contextPath = new URI(request.getRequestURI()).getPath();
        } catch (URISyntaxException e) {
        }

        Map<String, String> responseDirectives = new HashMap<String, String>();
        responseDirectives.put(REALM, properties.getRealm());
        responseDirectives.put(DOMAIN, contextPath);
        responseDirectives.put(NONCE, nonce.getValue());
        responseDirectives.put(ALGORITHM, StringUtils.join(properties.getAlgorithms(), ','));
        if (CollectionUtils.isNotEmpty(properties.getQopModes())) {
            responseDirectives.put(QOP, StringUtils.join(properties.getQopModes(), ','));
        }
        if (StringUtils.isNotBlank(nonce.getOpaque())) {
            responseDirectives.put(OPAQUE, nonce.getOpaque());
        }
        if (status == Status.STALE) {
            responseDirectives.put(STALE, "true");
        }

        /*
         * Build up the WWW-Authenticate header to be sent back in the response.
         */
        StringBuilder digestBuilder = new StringBuilder("Digest ");
        for (Iterator<Entry<String, String>> it = responseDirectives.entrySet().iterator(); it.hasNext();) {
            Entry<String, String> entry = it.next();
            digestBuilder.append(entry.getKey());
            digestBuilder.append("=\"");
            digestBuilder.append(entry.getValue());
            digestBuilder.append('"');
            if (it.hasNext()) {
                digestBuilder.append(", ");
            }
        }
        return AuthenticationResult.Challenged(digestBuilder.toString());
    }

    /**
     * Iterates through all nonces in the cache and removes any that are expired.
     */
    private void cleanupNonces() {
        Set<String> keySet = nonceMap.keySet();
        for (String nonceString : keySet.toArray(new String[keySet.size()])) {
            Nonce nonce = nonceMap.get(nonceString);
            if (nonce != null && nonce.isExpired()) {
                nonceMap.remove(nonceString);
            }
        }
    }

    /**
     * Calculates the MD5 digest for all objects passed in, concatenated with ':'. InputStreams will
     * be read in and updated directly via a byte buffer. Any other object will be converted to a
     * String updated with the equivalent ISO-8859-1 representation.
     */
    private String digest(Object... parts) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i] instanceof InputStream) {
                InputStream is = (InputStream) parts[i];
                byte[] buffer = new byte[1024];
                int len;
                while ((len = IOUtils.read(is, buffer, 0, buffer.length)) > 0) {
                    md.update(buffer, 0, len);
                }
            } else if (parts[i] instanceof byte[]) {
                md.update((byte[]) parts[i]);
            } else {
                md.update(String.valueOf(parts[i]).getBytes(StandardCharsets.ISO_8859_1));
            }

            if (i < parts.length - 1) {
                md.update((byte) ':');
            }
        }

        return TypeUtil.toString(md.digest(), 16);
    }

    private DigestHttpAuthProperties getReplacedProperties(RequestInfo request) {
        DigestHttpAuthProperties properties = new DigestHttpAuthProperties((DigestHttpAuthProperties) provider.getProperties());
        String channelId = provider.getConnector().getChannelId();
        String channelName = provider.getConnector().getChannel().getName();
        Map<String, Object> map = new HashMap<String, Object>();
        request.populateMap(map);

        properties.setRealm(replacer.replaceValues(properties.getRealm(), channelId, channelName, map));
        properties.setOpaque(replacer.replaceValues(properties.getOpaque(), channelId, channelName, map));

        Map<String, String> credentials = new LinkedHashMap<String, String>();
        for (Entry<String, String> entry : properties.getCredentials().entrySet()) {
            String username = replacer.replaceValues(entry.getKey(), channelId, channelName, map);
            if (StringUtils.isNotBlank(username)) {
                credentials.put(username, replacer.replaceValues(entry.getValue(), channelId, channelName, map));
            }
        }
        properties.setCredentials(credentials);

        return properties;
    }

    private enum Status {
        VALID, INVALID, STALE;
    }

    private class Nonce {

        private String value;
        private String opaque;
        private long created;
        private long count;

        public Nonce(String opaque) {
            this.opaque = StringUtils.trimToEmpty(opaque);
            byte[] buffer = new byte[24];
            rng.nextBytes(buffer);
            value = new String(Base64.encodeBase64(buffer), StandardCharsets.ISO_8859_1);
            created = System.nanoTime();
            count = 0;
        }

        public String getValue() {
            return value;
        }

        public String getOpaque() {
            return opaque;
        }

        public boolean isExpired() {
            return System.nanoTime() - created > MAX_NONCE_AGE;
        }

        public synchronized Status incrementCount() {
            count++;
            return count <= MAX_NONCE_COUNT ? Status.VALID : Status.STALE;
        }

        public synchronized Status updateCount(long count) {
            if (count <= this.count) {
                return Status.INVALID;
            }
            this.count = count;
            if (this.count > MAX_NONCE_COUNT) {
                return Status.STALE;
            }
            return Status.VALID;
        }
    }
}