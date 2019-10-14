/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.BufferedInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.file.FileConnectorException;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.S3SchemeProperties;
import com.mirth.connect.connectors.file.filters.RegexFilenameFilter;
import com.mirth.connect.userutil.MessageHeaders;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.S3Response;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;

public class S3Connection implements FileSystemConnection {

    static final String DELIMITER = "/";

    public class S3FileInfo implements FileInfo {

        private String bucketName;
        private S3Object s3Object;
        private String parent;
        private String name;

        public S3FileInfo(String bucketName, S3Object s3Object) {
            this.bucketName = bucketName;
            this.s3Object = s3Object;
            parent = bucketName;
            name = s3Object.key();

            if (StringUtils.contains(s3Object.key(), DELIMITER)) {
                int index = s3Object.key().lastIndexOf(DELIMITER);
                parent += DELIMITER + s3Object.key().substring(0, index);
                name = s3Object.key().substring(index + DELIMITER.length());
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getAbsolutePath() {
            return bucketName + DELIMITER + s3Object.key();
        }

        @Override
        public String getCanonicalPath() throws IOException {
            return getAbsolutePath();
        }

        @Override
        public String getParent() {
            return parent;
        }

        @Override
        public long getSize() {
            return s3Object.size();
        }

        @Override
        public long getLastModified() {
            return s3Object.lastModified().toEpochMilli();
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isFile() {
            return true;
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public void populateSourceMap(Map<String, Object> sourceMap) {
            addMetadataIfNotNull(sourceMap, "s3BucketName", bucketName);
            addMetadataIfNotNull(sourceMap, "s3ETag", unquote(s3Object.eTag()));
            addMetadataIfNotNull(sourceMap, "s3Key", s3Object.key());
            addMetadataIfNotNull(sourceMap, "s3Owner", s3Object.owner());
            addMetadataIfNotNull(sourceMap, "s3StorageClass", s3Object.storageClassAsString());
        }
    }

    private Logger logger = Logger.getLogger(getClass());

    FileSystemConnectionOptions fileSystemOptions;
    S3SchemeProperties schemeProps;
    S3ClientBuilder clientBuilder;
    S3Client client;
    StsClient sts;
    int stsDuration;

    public S3Connection(FileSystemConnectionOptions fileSystemOptions, int timeout) throws Exception {
        this.fileSystemOptions = fileSystemOptions;
        schemeProps = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

        clientBuilder = S3Client.builder();
        clientBuilder.httpClientBuilder(createClientConfiguration(timeout));

        if (isSTSEnabled()) {
            StsClientBuilder stsClientBuilder = StsClient.builder();
            stsClientBuilder.httpClientBuilder(createClientConfiguration(timeout));
            stsClientBuilder.credentialsProvider(createCredentialsProvider(fileSystemOptions));
            stsClientBuilder.region(Region.of(schemeProps.getRegion()));
            sts = stsClientBuilder.build();

            // Abide by AWS duration restrictions
            stsDuration = schemeProps.getDuration();
            if (stsDuration < 900) {
                stsDuration = 900;
            } else if (stsDuration > 129600) {
                stsDuration = 129600;
            }
        } else {
            clientBuilder.credentialsProvider(createCredentialsProvider(fileSystemOptions));
            clientBuilder.region(Region.of(schemeProps.getRegion()));
            client = clientBuilder.build();
        }
    }

    boolean isSTSEnabled() {
        return schemeProps.isUseTemporaryCredentials() && !fileSystemOptions.isAnonymous();
    }

    SdkHttpClient.Builder<ApacheHttpClient.Builder> createClientConfiguration(int timeout) {
        Duration timeoutDuration = Duration.ofMillis(timeout);
        return ApacheHttpClient.builder().connectionTimeout(timeoutDuration).socketTimeout(timeoutDuration);
    }

    AwsCredentialsProvider createCredentialsProvider(FileSystemConnectionOptions fileSystemOptions) {
        S3SchemeProperties schemeProps = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

        if (fileSystemOptions.isAnonymous()) {
            return AnonymousCredentialsProvider.create();
        } else if (schemeProps.isUseDefaultCredentialProviderChain() && StringUtils.isBlank(fileSystemOptions.getUsername()) && StringUtils.isBlank(fileSystemOptions.getPassword())) {
            return DefaultCredentialsProvider.create();
        } else {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(fileSystemOptions.getUsername(), fileSystemOptions.getPassword()));
        }
    }

    S3Client getClient() {
        if (isSTSEnabled() && client == null) {
            GetSessionTokenRequest getSessionTokenRequest = GetSessionTokenRequest.builder().durationSeconds(stsDuration).build();
            clientBuilder.credentialsProvider(StsGetSessionTokenCredentialsProvider.builder().stsClient(sts).refreshRequest(getSessionTokenRequest).build());
            clientBuilder.region(Region.of(schemeProps.getRegion()));
            client = clientBuilder.build();
        }

        return client;
    }

    Pair<String, String> getBucketNameAndPrefix(String fromDir) {
        String bucketName = null;
        String prefix = null;

        if (StringUtils.isNotBlank(fromDir)) {
            // Remove leading delimiters
            while (StringUtils.startsWith(fromDir, DELIMITER)) {
                fromDir = StringUtils.substring(fromDir, 1);
            }

            int index = StringUtils.indexOf(fromDir, DELIMITER);
            if (index > 0) {
                bucketName = StringUtils.substring(fromDir, 0, index);
                prefix = StringUtils.trimToNull(StringUtils.substring(fromDir, index + 1));
            } else {
                // No delimiters
                bucketName = fromDir;
            }
        }

        return new ImmutablePair<String, String>(bucketName, prefix);
    }

    String normalizeKey(String key, boolean leadingDelimiter, boolean trailingDelimiter) {
        if (key != null) {
            if (leadingDelimiter) {
                if (!StringUtils.startsWith(key, DELIMITER)) {
                    key = DELIMITER + key;
                }
            } else {
                while (StringUtils.startsWith(key, DELIMITER)) {
                    key = StringUtils.substring(key, 1);
                }
            }

            if (trailingDelimiter) {
                if (!StringUtils.endsWith(key, DELIMITER)) {
                    key += DELIMITER;
                }
            } else {
                while (StringUtils.endsWith(key, DELIMITER)) {
                    key = StringUtils.substring(key, 0, key.length() - 1);
                }
            }
        }

        return key;
    }

    ListObjectsV2Request.Builder createListRequest(String bucketName, String prefix) {
        return ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).delimiter(DELIMITER);
    }

    Map<String, String> getCustomHeaders() {
        Map<String, String> headers = new HashMap<String, String>();

        if (MapUtils.isNotEmpty(schemeProps.getCustomHeaders())) {
            for (Entry<String, List<String>> entry : schemeProps.getCustomHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    headers.put(entry.getKey(), value);
                }
            }
        }

        return headers;
    }

    void addMetadataIfNotNull(Map<String, Object> map, String key, Object value) {
        if (map != null && value != null) {
            map.put(key, value);
        }
    }

    void populateObjectMetadata(Map<String, Object> map, Map<String, List<String>> objectMetadata) {
        if (map != null) {
            map.put("s3Metadata", new MessageHeaders(objectMetadata));
        }
    }

    private String unquote(String str) {
        return StringUtils.removeEnd(StringUtils.removeStart(str, "\""), "\"");
    }

    @Override
    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        try {
            return doListFiles(fromDir, filenamePattern, isRegex, ignoreDot);
        } catch (AwsServiceException e) {
            handleException(e);
            return doListFiles(fromDir, filenamePattern, isRegex, ignoreDot);
        }
    }

    List<FileInfo> doListFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        String filePrefix = null;

        FilenameFilter filenameFilter;
        if (isRegex) {
            filenameFilter = new RegexFilenameFilter(filenamePattern);
        } else {
            String[] wildcards = filenamePattern.trim().split("\\s*,\\s*");

            // Take advantage of the S3 prefix option if the filename pattern is a simple ending wildcard
            if (wildcards.length == 1 && StringUtils.length(wildcards[0]) > 1 && StringUtils.indexOf(wildcards[0], "*") == wildcards[0].length() - 1) {
                filePrefix = wildcards[0].substring(0, wildcards[0].length() - 1);
            }

            filenameFilter = new WildcardFileFilter(wildcards);
        }

        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        S3Client client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String dirPrefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        ListObjectsV2Request.Builder requestBuilder = createListRequest(bucketName, dirPrefix);

        // Add the file prefix if necessary
        if (StringUtils.isNotBlank(filePrefix)) {
            requestBuilder.prefix(StringUtils.trimToEmpty(dirPrefix) + filePrefix);
        }

        ListObjectsV2Response result;

        do {
            result = client.listObjectsV2(requestBuilder.build());

            for (S3Object s3Object : result.contents()) {
                // Ignore the folder itself
                if (!StringUtils.equals(s3Object.key(), dirPrefix)) {
                    S3FileInfo fileInfo = new S3FileInfo(bucketName, s3Object);

                    // Validate against the filter before adding to the list 
                    if ((filenameFilter == null || filenameFilter.accept(null, fileInfo.getName())) && (!ignoreDot || !StringUtils.startsWith(fileInfo.getName(), "."))) {
                        fileInfoList.add(fileInfo);
                    }
                }
            }

            requestBuilder.continuationToken(result.nextContinuationToken());
        } while (result.isTruncated());

        return fileInfoList;
    }

    @Override
    public List<String> listDirectories(String fromDir) throws Exception {
        try {
            return doListDirectories(fromDir);
        } catch (AwsServiceException e) {
            handleException(e);
            return doListDirectories(fromDir);
        }
    }

    List<String> doListDirectories(String fromDir) throws Exception {
        List<String> directories = new ArrayList<String>();
        S3Client client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        ListObjectsV2Request.Builder requestBuilder = createListRequest(bucketName, prefix);
        ListObjectsV2Response result;

        do {
            result = client.listObjectsV2(requestBuilder.build());

            for (CommonPrefix commonPrefix : result.commonPrefixes()) {
                directories.add(bucketName + DELIMITER + commonPrefix.prefix());
            }

            requestBuilder.continuationToken(result.nextContinuationToken());
        } while (result.isTruncated());

        return directories;
    }

    @Override
    public boolean exists(String file, String path) throws Exception {
        try {
            return doExists(file, path);
        } catch (AwsServiceException e) {
            handleException(e);
            return doExists(file, path);
        }
    }

    boolean doExists(String file, String path) throws Exception {
        S3Client client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(path);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        try {
            HeadObjectResponse response = client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
            return response != null && (response.deleteMarker() == null || !response.deleteMarker());
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public InputStream readFile(String file, String fromDir, Map<String, Object> sourceMap) throws Exception {
        try {
            return doReadFile(file, fromDir, sourceMap);
        } catch (AwsServiceException e) {
            handleException(e);
            return doReadFile(file, fromDir, sourceMap);
        }
    }

    InputStream doReadFile(String file, String fromDir, Map<String, Object> sourceMap) throws Exception {
        S3Client client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();

        CustomS3Response<GetObjectResponse> response = client.getObject(request, new CustomResponseTransformer<GetObjectResponse>());

        populateObjectMetadata(sourceMap, response.getResponse().sdkHttpResponse().headers());

        return response.getData();
    }

    @Override
    public void closeReadFile() throws Exception {}

    @Override
    public boolean canAppend() {
        // S3 doesn't allow appending
        return false;
    }

    @Override
    public void writeFile(String file, String toDir, boolean append, InputStream message, long contentLength, Map<String, Object> connectorMap) throws Exception {
        try {
            doWriteFile(file, toDir, append, message, contentLength, connectorMap);
        } catch (AwsServiceException e) {
            handleException(e);
            doWriteFile(file, toDir, append, message, contentLength, connectorMap);
        }
    }

    void doWriteFile(String file, String toDir, boolean append, InputStream message, long contentLength, Map<String, Object> connectorMap) throws Exception {
        S3Client client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(toDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucketName).key(key).metadata(getCustomHeaders()).build();
        PutObjectResponse result = client.putObject(putRequest, RequestBody.fromInputStream(message, contentLength));

        if (connectorMap != null) {
            addMetadataIfNotNull(connectorMap, "s3ETag", unquote(result.eTag()));
            addMetadataIfNotNull(connectorMap, "s3ExpirationTime", result.expiration());
            addMetadataIfNotNull(connectorMap, "s3SSEAlgorithm", result.serverSideEncryptionAsString());
            addMetadataIfNotNull(connectorMap, "s3SSECustomerAlgorithm", result.sseCustomerAlgorithm());
            addMetadataIfNotNull(connectorMap, "s3SSECustomerKeyMd5", result.sseCustomerKeyMD5());
            addMetadataIfNotNull(connectorMap, "s3VersionId", result.versionId());
        }
    }

    @Override
    public void delete(String file, String fromDir, boolean mayNotExist) throws Exception {
        try {
            doDelete(file, fromDir, mayNotExist);
        } catch (AwsServiceException e) {
            handleException(e);
            doDelete(file, fromDir, mayNotExist);
        }
    }

    void doDelete(String file, String fromDir, boolean mayNotExist) throws Exception {
        S3Client client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

        client.deleteObject(deleteRequest);

        if (mayNotExist && exists(file, fromDir)) {
            throw new FileConnectorException("File should not exist after deleting, bucket: " + fromDir + ", file: " + file);
        }
    }

    @Override
    public void move(String fromName, String fromDir, String toName, String toDir) throws Exception {
        try {
            doMove(fromName, fromDir, toName, toDir);
        } catch (AwsServiceException e) {
            handleException(e);
            doMove(fromName, fromDir, toName, toDir);
        }
    }

    void doMove(String fromName, String fromDir, String toName, String toDir) throws Exception {
        S3Client client = getClient();

        Pair<String, String> fromBucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String fromBucketName = fromBucketNameAndPrefix.getLeft();
        String fromPrefix = normalizeKey(fromBucketNameAndPrefix.getRight(), false, true);

        String fromKey = fromName;
        if (StringUtils.isNotBlank(fromPrefix) && !StringUtils.equals(fromPrefix, DELIMITER)) {
            fromKey = fromPrefix + fromKey;
        }

        Pair<String, String> toBucketNameAndPrefix = getBucketNameAndPrefix(toDir);
        String toBucketName = toBucketNameAndPrefix.getLeft();
        String toPrefix = normalizeKey(toBucketNameAndPrefix.getRight(), false, true);

        String toKey = toName;
        if (StringUtils.isNotBlank(toPrefix) && !StringUtils.equals(toPrefix, DELIMITER)) {
            toKey = toPrefix + toKey;
        }

        try {
            if (fileSystemOptions.isAnonymous()) {
                GetObjectRequest getRequest = GetObjectRequest.builder().bucket(fromBucketName).key(fromKey).build();
                CustomS3Response<GetObjectResponse> response = client.getObject(getRequest, new CustomResponseTransformer<GetObjectResponse>());

                try {
                    PutObjectRequest putRequest = PutObjectRequest.builder().bucket(toBucketName).key(toKey).metadata(getCustomHeaders()).build();
                    client.putObject(putRequest, RequestBody.fromInputStream(new BufferedInputStream(response.getData()), response.getResponse().contentLength()));
                } finally {
                    IOUtils.closeQuietly(response.getData());
                }
            } else {
                String fromUrl = URLEncoder.encode(fromBucketName + DELIMITER + fromKey, StandardCharsets.UTF_8.toString());
                client.copyObject(CopyObjectRequest.builder().copySource(fromUrl).bucket(toBucketName).key(toKey).build());
            }

            // delete original
            delete(fromName, fromDir, false);
        } catch (Exception e) {
            throw new FileConnectorException("Error moving file from [bucket: " + fromBucketName + ", key: " + fromKey + "] to [bucket: " + toBucketName + ", key: " + toKey + "]", e);
        }
    }

    private class CustomS3Response<T extends S3Response> {
        private T response;
        private InputStream data;

        public CustomS3Response(T response, InputStream data) {
            this.response = response;
            this.data = data;
        }

        public T getResponse() {
            return response;
        }

        public InputStream getData() {
            return data;
        }
    }

    private class CustomResponseTransformer<T extends S3Response> implements ResponseTransformer<T, CustomS3Response<T>> {
        @Override
        public CustomS3Response<T> transform(T response, AbortableInputStream inputStream) throws Exception {
            return new CustomS3Response<T>(response, inputStream);
        }

        @Override
        public boolean needsConnectionLeftOpen() {
            return true;
        }
    }

    @Override
    public boolean isConnected() {
        return isValid();
    }

    @Override
    public void disconnect() {}

    @Override
    public void activate() {}

    @Override
    public void passivate() {}

    @Override
    public void destroy() {
        if (client != null) {
            client.close();
        }
        if (sts != null) {
            sts.close();
        }
    }

    @Override
    public boolean isValid() {
        return client != null || sts != null;
    }

    @Override
    public boolean canRead(String readDir) {
        try {
            S3Client client = getClient();

            Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(readDir);
            String bucketName = bucketNameAndPrefix.getLeft();
            String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, false);
            String prefixWithTrailingDelimiter = normalizeKey(bucketNameAndPrefix.getRight(), false, true);
            CommonPrefix commonPrefixWithTrailingDelimiter = CommonPrefix.builder().prefix(prefixWithTrailingDelimiter).build();

            ListObjectsV2Request.Builder requestBuilder = createListRequest(bucketName, prefix);
            ListObjectsV2Response result;

            do {
                result = client.listObjectsV2(requestBuilder.build());

                if (StringUtils.isBlank(prefixWithTrailingDelimiter) || result.commonPrefixes().contains(commonPrefixWithTrailingDelimiter)) {
                    return true;
                }

                requestBuilder.continuationToken(result.nextContinuationToken());
            } while (result.isTruncated());
        } catch (Exception e) {
            logger.debug("Exception while attempting to read from S3 location \"" + readDir + "\".", e);
        }

        return false;
    }

    @Override
    public boolean canWrite(String writeDir) {
        /*
         * There's no foolproof way via the AWS SDK to determine whether objects can be written,
         * other than actually trying to put an object. But there's no guarantee that we'll be able
         * to delete the object after putting it, so instead of doing that we just delegate to
         * canRead. It's not perfect and may produce false negatives if the bucket policy allows
         * puts but not listing. But it's something.
         */
        return canRead(writeDir);
    }

    void handleException(AwsServiceException e) throws AwsServiceException {
        if (isSTSEnabled() && e.awsErrorDetails() != null && StringUtils.equals(e.awsErrorDetails().errorCode(), "ExpiredToken")) {
            if (client != null) {
                client.close();
                client = null;
            }
        } else {
            throw e;
        }
    }
}
