/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.S3SchemeProperties;
import com.mirth.connect.userutil.MessageHeaders;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;
import software.amazon.awssdk.services.sts.model.GetSessionTokenResponse;

public class S3ConnectionTest {

    static S3Client client;
    static StsClient sts;

    @BeforeClass
    public static void setup() throws Exception {
        client = spy(S3Client.class);

        sts = spy(StsClient.class);

        GetSessionTokenResponse sessionTokenResult = GetSessionTokenResponse.builder().credentials(Credentials.builder().accessKeyId("accessKeyId").secretAccessKey("secretAccessKey").sessionToken("sessionToken").expiration(Instant.now().plusSeconds(60 * 60)).build()).build();
        doReturn(sessionTokenResult).when(sts).getSessionToken(any(GetSessionTokenRequest.class));
    }

    @Test
    public void testIsSTSEnabled() throws Exception {
        S3Connection s3Conn = getConnection(false, null, null, true, false);
        assertFalse(s3Conn.isSTSEnabled());

        s3Conn = getConnection(false, null, null, true, true);
        assertTrue(s3Conn.isSTSEnabled());

        s3Conn = getConnection(true, null, null, true, false);
        assertFalse(s3Conn.isSTSEnabled());

        s3Conn = getConnection(true, null, null, true, true);
        assertFalse(s3Conn.isSTSEnabled());
    }

    @Test
    public void testCreateCredentialsProvider() throws Exception {
        S3Connection s3Conn = getConnection();

        // Anonymous with DCPC
        FileSystemConnectionOptions fileSystemOptions = getOptions(true, "user", "pass", true, true);
        AwsCredentialsProvider provider = s3Conn.createCredentialsProvider(fileSystemOptions);
        assertEquals(AnonymousCredentialsProvider.class, provider.getClass());
        assertEquals(AwsBasicCredentials.class, provider.resolveCredentials().getClass());

        // Anonymous without DCPC
        fileSystemOptions = getOptions(true, null, null, false, false);
        provider = s3Conn.createCredentialsProvider(fileSystemOptions);
        assertEquals(AnonymousCredentialsProvider.class, provider.getClass());
        assertEquals(AwsBasicCredentials.class, provider.resolveCredentials().getClass());

        // Null credentials with DCPC
        fileSystemOptions = getOptions(false, null, null, true, true);
        provider = s3Conn.createCredentialsProvider(fileSystemOptions);
        assertEquals(DefaultCredentialsProvider.class, provider.getClass());

        // Blank credentials with DCPC
        fileSystemOptions = getOptions(false, " ", " ", true, true);
        provider = s3Conn.createCredentialsProvider(fileSystemOptions);
        assertEquals(DefaultCredentialsProvider.class, provider.getClass());

        // Null credentials without DCPC
        String accessKeyId = null;
        String secretKeyId = null;
        fileSystemOptions = getOptions(false, accessKeyId, secretKeyId, false, true);
        try {
            provider = s3Conn.createCredentialsProvider(fileSystemOptions);
            fail("NullPointerException should have been thrown");
        } catch (NullPointerException e) {
        }

        // Blank credentials without DCPC
        accessKeyId = " ";
        secretKeyId = " ";
        fileSystemOptions = getOptions(false, accessKeyId, secretKeyId, false, true);
        try {
            provider = s3Conn.createCredentialsProvider(fileSystemOptions);
            fail("NullPointerException should have been thrown");
        } catch (NullPointerException e) {
        }

        // Valid credentials with DCPC
        accessKeyId = "user";
        secretKeyId = "pass";
        fileSystemOptions = getOptions(false, accessKeyId, secretKeyId, true, true);
        provider = s3Conn.createCredentialsProvider(fileSystemOptions);
        assertEquals(StaticCredentialsProvider.class, provider.getClass());
        assertEquals(AwsBasicCredentials.class, provider.resolveCredentials().getClass());
        assertEquals(accessKeyId, provider.resolveCredentials().accessKeyId());
        assertEquals(secretKeyId, provider.resolveCredentials().secretAccessKey());

        // Valid credentials without DCPC
        accessKeyId = "user";
        secretKeyId = "pass";
        fileSystemOptions = getOptions(false, accessKeyId, secretKeyId, false, true);
        provider = s3Conn.createCredentialsProvider(fileSystemOptions);
        assertEquals(StaticCredentialsProvider.class, provider.getClass());
        assertEquals(AwsBasicCredentials.class, provider.resolveCredentials().getClass());
        assertEquals(accessKeyId, provider.resolveCredentials().accessKeyId());
        assertEquals(secretKeyId, provider.resolveCredentials().secretAccessKey());
    }

    @Test
    public void testGetClient() throws Exception {
        // Anonymous without STS
        S3Connection s3Conn = getConnection(true, null, null, false, false);
        assertEquals(client, s3Conn.getClient());

        // Anonymous with STS
        s3Conn = getConnection(true, null, null, false, true);
        assertEquals(client, s3Conn.getClient());

        // Credentials without STS
        s3Conn = getConnection(false, "user", "pass", false, false);
        assertEquals(client, s3Conn.getClient());

        // Credentials with STS, client not null
        s3Conn = getConnection(false, "user", "pass", false, true);
        assertEquals(client, s3Conn.getClient());

        // Credentials with STS, client null
        s3Conn = getConnection(false, "user", "pass", false, true);
        s3Conn.client = null;
        assertNotNull(s3Conn.getClient());
    }

    @Test
    public void testGetBucketNameAndPrefix() throws Exception {
        S3Connection s3Conn = getConnection();

        Pair<String, String> bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix(null);
        assertNull(bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix(" ");
        assertNull(bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("/");
        assertEquals("", bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("test");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("/test");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("/test/");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("test/");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertNull(bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("test/dir");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertEquals("dir", bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("/test/dir");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertEquals("dir", bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("/test/dir/");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertEquals("dir/", bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("test/dir/dir2");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertEquals("dir/dir2", bucketNameAndPrefix.getRight());

        bucketNameAndPrefix = s3Conn.getBucketNameAndPrefix("test/dir/dir2/");
        assertEquals("test", bucketNameAndPrefix.getLeft());
        assertEquals("dir/dir2/", bucketNameAndPrefix.getRight());
    }

    @Test
    public void testNormalizeKey() throws Exception {
        S3Connection s3Conn = getConnection();

        String key = s3Conn.normalizeKey(null, false, false);
        assertNull(key);

        key = s3Conn.normalizeKey("", false, false);
        assertEquals("", key);

        key = s3Conn.normalizeKey("", true, false);
        assertEquals("", key);

        key = s3Conn.normalizeKey("", false, true);
        assertEquals("/", key);

        key = s3Conn.normalizeKey("", true, true);
        assertEquals("/", key);

        key = s3Conn.normalizeKey("test", false, false);
        assertEquals("test", key);

        key = s3Conn.normalizeKey("test", true, false);
        assertEquals("/test", key);

        key = s3Conn.normalizeKey("test", false, true);
        assertEquals("test/", key);

        key = s3Conn.normalizeKey("test", true, true);
        assertEquals("/test/", key);

        key = s3Conn.normalizeKey("/test", false, false);
        assertEquals("test", key);

        key = s3Conn.normalizeKey("/test", false, true);
        assertEquals("test/", key);

        key = s3Conn.normalizeKey("/test/", false, false);
        assertEquals("test", key);
    }

    @Test
    public void testCreateListRequest() throws Exception {
        S3Connection s3Conn = getConnection();

        ListObjectsV2Request request = s3Conn.createListRequest("test", null).build();
        assertEquals("test", request.bucket());
        assertNull(request.prefix());
        assertEquals("/", request.delimiter());

        request = s3Conn.createListRequest("test", "").build();
        assertEquals("test", request.bucket());
        assertEquals("", request.prefix());

        request = s3Conn.createListRequest("test", "prefix").build();
        assertEquals("test", request.bucket());
        assertEquals("prefix", request.prefix());

        request = s3Conn.createListRequest("test", "prefix/dir").build();
        assertEquals("test", request.bucket());
        assertEquals("prefix/dir", request.prefix());
    }

    @Test
    public void testGetCustomHeaders() throws Exception {
        S3Connection s3Conn = getConnection();
        s3Conn.schemeProps.getCustomHeaders().put("X-Custom-Header", Collections.singletonList("Value"));
        assertEquals("Value", s3Conn.getCustomHeaders().get("X-Custom-Header"));
    }

    @Test
    public void testAddMetadataIfNotNull() throws Exception {
        S3Connection s3Conn = getConnection();

        s3Conn.addMetadataIfNotNull(null, null, null);

        Map<String, Object> map = new HashMap<String, Object>();
        s3Conn.addMetadataIfNotNull(map, null, null);
        assertTrue(MapUtils.isEmpty(map));

        map = new HashMap<String, Object>();
        s3Conn.addMetadataIfNotNull(map, "key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    public void testPopulateObjectMetadata() throws Exception {
        S3Connection s3Conn = getConnection();

        s3Conn.populateObjectMetadata(null, null);

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, List<String>> objectMetadata = new HashMap<String, List<String>>();
        s3Conn.populateObjectMetadata(map, objectMetadata);
        assertTrue(CollectionUtils.isEmpty(((MessageHeaders) map.get("s3Metadata")).getKeys()));

        map = new HashMap<String, Object>();
        objectMetadata = new HashMap<String, List<String>>();
        objectMetadata.put("key", Collections.singletonList("value"));
        s3Conn.populateObjectMetadata(map, objectMetadata);
        assertEquals("value", ((MessageHeaders) map.get("s3Metadata")).getHeader("key"));
    }

    @Test
    public void testListFiles() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doReturn(null).when(s3Conn).doListFiles(anyString(), anyString(), anyBoolean(), anyBoolean());
        s3Conn.listFiles("test/dir", "*", true, true);

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doListFiles(anyString(), anyString(), anyBoolean(), anyBoolean());
        try {
            s3Conn.listFiles("test/dir", "*", true, true);
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doListFiles(anyString(), anyString(), anyBoolean(), anyBoolean());
        s3Conn.listFiles("test/dir", "*", true, true);
    }

    @Test
    public void testListDirectories() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doReturn(null).when(s3Conn).doListDirectories(anyString());
        s3Conn.listDirectories("dir");

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doListDirectories(anyString());
        try {
            s3Conn.listDirectories("dir");
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doListDirectories(anyString());
        s3Conn.listDirectories("dir");
    }

    @Test
    public void testExists() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doReturn(true).when(s3Conn).doExists(anyString(), anyString());
        s3Conn.exists("dir", "path");

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doExists(anyString(), anyString());
        try {
            s3Conn.exists("dir", "path");
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doExists(anyString(), anyString());
        s3Conn.exists("dir", "path");
    }

    @Test
    public void testReadFile() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doReturn(null).when(s3Conn).doReadFile(anyString(), anyString(), isNull());
        s3Conn.readFile("file", "dir", null);

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doReadFile(anyString(), anyString(), isNull());
        try {
            s3Conn.readFile("file", "dir", null);
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doReadFile(anyString(), anyString(), isNull());
        s3Conn.readFile("file", "dir", null);
    }

    @Test
    public void testWriteFile() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doNothing().when(s3Conn).doWriteFile(anyString(), anyString(), anyBoolean(), isNull(), anyLong(), isNull());
        s3Conn.writeFile("file", "dir", false, null, 0, null);

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doWriteFile(anyString(), anyString(), anyBoolean(), isNull(), anyLong(), isNull());
        try {
            s3Conn.writeFile("file", "dir", false, null, 0, null);
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doWriteFile(anyString(), anyString(), anyBoolean(), isNull(), anyLong(), isNull());
        s3Conn.writeFile("file", "dir", false, null, 0, null);
    }

    @Test
    public void testDelete() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doNothing().when(s3Conn).doDelete(anyString(), anyString(), anyBoolean());
        s3Conn.delete("file", "dir", false);

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doDelete(anyString(), anyString(), anyBoolean());
        try {
            s3Conn.delete("file", "dir", false);
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doDelete(anyString(), anyString(), anyBoolean());
        s3Conn.delete("file", "dir", false);
    }

    @Test
    public void testMove() throws Exception {
        S3Connection s3Conn = spy(getConnection());
        doNothing().when(s3Conn).doMove(anyString(), anyString(), anyString(), anyString());
        s3Conn.move("fromName", "fromDir", "toName", "toDir");

        s3Conn = spy(getConnection());
        doAnswer(answer()).when(s3Conn).doMove(anyString(), anyString(), anyString(), anyString());
        try {
            s3Conn.move("fromName", "fromDir", "toName", "toDir");
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
        }

        s3Conn = spy(getConnection(false, "user", "pass", true, true));
        doAnswer(answer()).when(s3Conn).doMove(anyString(), anyString(), anyString(), anyString());
        s3Conn.move("fromName", "fromDir", "toName", "toDir");
    }

    @Test
    public void testIsValidIsConnected() throws Exception {
        S3Connection s3Conn = getConnection();

        s3Conn.client = null;
        s3Conn.sts = null;
        assertFalse(s3Conn.isValid());
        assertFalse(s3Conn.isConnected());

        s3Conn.client = client;
        s3Conn.sts = null;
        assertTrue(s3Conn.isValid());
        assertTrue(s3Conn.isConnected());

        s3Conn.client = null;
        s3Conn.sts = sts;
        assertTrue(s3Conn.isValid());
        assertTrue(s3Conn.isConnected());

        s3Conn.client = client;
        s3Conn.sts = sts;
        assertTrue(s3Conn.isValid());
        assertTrue(s3Conn.isConnected());
    }

    @Test
    public void testDestroy() throws Exception {
        S3Connection s3Conn = getConnection();

        S3Client client = mock(S3Client.class);
        StsClient sts = mock(StsClient.class);
        s3Conn.client = client;
        s3Conn.sts = sts;
        s3Conn.destroy();
        verify(client, atLeastOnce()).close();
        verify(sts, atLeastOnce()).close();

        sts = mock(StsClient.class);
        s3Conn.client = null;
        s3Conn.sts = sts;
        s3Conn.destroy();
        verify(sts, atLeastOnce()).close();
    }

    @Test
    public void testHandleException() throws Exception {
        // Anonymous with STS
        S3Connection s3Conn = getConnection(true, null, null, true, true);
        AwsServiceException err = AwsServiceException.builder().message("").build();
        try {
            s3Conn.handleException(err);
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
            assertEquals(err, e);
        }

        // Credentials with STS, not expired
        s3Conn = getConnection(false, "user", "pass", true, true);
        err = AwsServiceException.builder().message("").build();
        try {
            s3Conn.handleException(err);
            fail("Exception should have been thrown");
        } catch (AwsServiceException e) {
            assertEquals(err, e);
        }

        // Credentials with STS, expired
        s3Conn = getConnection(false, "user", "pass", true, true);
        S3Client client = mock(S3Client.class);
        s3Conn.client = client;
        err = AwsServiceException.builder().message("").awsErrorDetails(AwsErrorDetails.builder().errorCode("ExpiredToken").build()).build();
        s3Conn.handleException(err);
        verify(client, atLeastOnce()).close();
    }

    private S3Connection getConnection() throws Exception {
        return getConnection(true, null, null, true, false);
    }

    private S3Connection getConnection(boolean anonymous, String username, String password, boolean useDefaultCredentialProviderChain, boolean useTemporaryCredentials) throws Exception {
        return getConnection(anonymous, username, password, useDefaultCredentialProviderChain, useTemporaryCredentials, 7200, Region.US_WEST_2.id());
    }

    private S3Connection getConnection(boolean anonymous, String username, String password, boolean useDefaultCredentialProviderChain, boolean useTemporaryCredentials, int duration, String region) throws Exception {
        FileSystemConnectionOptions fileSystemOptions = getOptions(anonymous, username, password, useDefaultCredentialProviderChain, useTemporaryCredentials, duration, region);
        S3Connection s3Conn = new S3Connection(fileSystemOptions, 10000);
        s3Conn.client = client;
        s3Conn.sts = sts;
        return s3Conn;
    }

    private FileSystemConnectionOptions getOptions(boolean anonymous, String username, String password, boolean useDefaultCredentialProviderChain, boolean useTemporaryCredentials) {
        return getOptions(anonymous, username, password, useDefaultCredentialProviderChain, useTemporaryCredentials, 7200, Region.US_WEST_2.id());
    }

    private FileSystemConnectionOptions getOptions(boolean anonymous, String username, String password, boolean useDefaultCredentialProviderChain, boolean useTemporaryCredentials, int duration, String region) {
        S3SchemeProperties schemeProps = new S3SchemeProperties();
        schemeProps.setUseDefaultCredentialProviderChain(useDefaultCredentialProviderChain);
        schemeProps.setUseTemporaryCredentials(useTemporaryCredentials);
        schemeProps.setDuration(duration);
        schemeProps.setRegion(region);

        return new FileSystemConnectionOptions(anonymous, username, password, schemeProps);
    }

    private <T> Answer<T> answer() {
        return new Answer<T>() {
            private int count;

            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                count++;
                if (count > 1) {
                    return null;
                } else {
                    throw AwsServiceException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("ExpiredToken").build()).build();
                }
            }
        };
    }
}
