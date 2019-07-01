/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.mirth.connect.connectors.file.FileConnectorException;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.S3SchemeProperties;
import com.mirth.connect.connectors.file.filters.RegexFilenameFilter;
import com.mirth.connect.userutil.MessageHeaders;

public class S3Connection implements FileSystemConnection {

    static final String DELIMITER = "/";

    public class S3FileInfo implements FileInfo {

        private S3ObjectSummary summary;
        private String parent;
        private String name;

        public S3FileInfo(S3ObjectSummary summary) {
            this.summary = summary;
            parent = summary.getBucketName();
            name = summary.getKey();

            if (StringUtils.contains(summary.getKey(), DELIMITER)) {
                int index = summary.getKey().lastIndexOf(DELIMITER);
                parent += DELIMITER + summary.getKey().substring(0, index);
                name = summary.getKey().substring(index + DELIMITER.length());
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getAbsolutePath() {
            return summary.getBucketName() + DELIMITER + summary.getKey();
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
            return summary.getSize();
        }

        @Override
        public long getLastModified() {
            return summary.getLastModified().getTime();
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
            addMetadataIfNotNull(sourceMap, "s3BucketName", summary.getBucketName());
            addMetadataIfNotNull(sourceMap, "s3ETag", summary.getETag());
            addMetadataIfNotNull(sourceMap, "s3Key", summary.getKey());
            addMetadataIfNotNull(sourceMap, "s3Owner", summary.getOwner());
            addMetadataIfNotNull(sourceMap, "s3StorageClass", summary.getStorageClass());
        }
    }

    private Logger logger = Logger.getLogger(getClass());

    FileSystemConnectionOptions fileSystemOptions;
    S3SchemeProperties schemeProps;
    AmazonS3ClientBuilder clientBuilder;
    AmazonS3 client;
    AWSSecurityTokenService sts;
    int stsDuration;

    public S3Connection(FileSystemConnectionOptions fileSystemOptions, int timeout) throws Exception {
        this.fileSystemOptions = fileSystemOptions;
        schemeProps = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

        clientBuilder = AmazonS3ClientBuilder.standard();
        clientBuilder.setClientConfiguration(createClientConfiguration(timeout));

        if (isSTSEnabled()) {
            AWSSecurityTokenServiceClientBuilder stsClientBuilder = AWSSecurityTokenServiceClientBuilder.standard();
            stsClientBuilder.setClientConfiguration(createClientConfiguration(timeout));
            stsClientBuilder.setCredentials(createCredentialsProvider(fileSystemOptions));
            stsClientBuilder.setRegion(schemeProps.getRegion());
            sts = stsClientBuilder.build();

            // Abide by AWS duration restrictions
            stsDuration = schemeProps.getDuration();
            if (stsDuration < 900) {
                stsDuration = 900;
            } else if (stsDuration > 129600) {
                stsDuration = 129600;
            }
        } else {
            clientBuilder.setCredentials(createCredentialsProvider(fileSystemOptions));
            clientBuilder.setRegion(schemeProps.getRegion());
            client = clientBuilder.build();
        }
    }

    boolean isSTSEnabled() {
        return schemeProps.isUseTemporaryCredentials() && !fileSystemOptions.isAnonymous();
    }

    ClientConfiguration createClientConfiguration(int timeout) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(timeout);
        clientConfiguration.setSocketTimeout(timeout);
        return clientConfiguration;
    }

    AWSCredentialsProvider createCredentialsProvider(FileSystemConnectionOptions fileSystemOptions) {
        S3SchemeProperties schemeProps = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

        if (fileSystemOptions.isAnonymous()) {
            return new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        } else if (schemeProps.isUseDefaultCredentialProviderChain() && StringUtils.isBlank(fileSystemOptions.getUsername()) && StringUtils.isBlank(fileSystemOptions.getPassword())) {
            return new DefaultAWSCredentialsProviderChain();
        } else {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(fileSystemOptions.getUsername(), fileSystemOptions.getPassword()));
        }
    }

    AmazonS3 getClient() {
        if (isSTSEnabled() && client == null) {
            GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest();
            getSessionTokenRequest.setDurationSeconds(stsDuration);

            GetSessionTokenResult sessionTokenResult = sts.getSessionToken(getSessionTokenRequest);
            Credentials sessionCredentials = sessionTokenResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(), sessionCredentials.getSessionToken());
            clientBuilder.setCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials));
            clientBuilder.setRegion(schemeProps.getRegion());
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

    ListObjectsV2Request createListRequest(String bucketName, String prefix) {
        return new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix).withDelimiter(DELIMITER);
    }

    void addCustomHeaders(AmazonWebServiceRequest request) {
        if (MapUtils.isNotEmpty(schemeProps.getCustomHeaders())) {
            for (Entry<String, List<String>> entry : schemeProps.getCustomHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    request.putCustomRequestHeader(entry.getKey(), value);
                }
            }
        }
    }

    void addMetadataIfNotNull(Map<String, Object> map, String key, Object value) {
        if (map != null && value != null) {
            map.put(key, value);
        }
    }

    void populateObjectMetadata(Map<String, Object> map, ObjectMetadata objectMetadata) {
        if (map != null) {
            Map<String, List<String>> metadata = new HashMap<String, List<String>>();
            for (Entry<String, Object> entry : objectMetadata.getRawMetadata().entrySet()) {
                List<String> list = metadata.get(entry.getKey());
                if (list == null) {
                    list = new ArrayList<String>();
                    metadata.put(entry.getKey(), list);
                }
                list.add(String.valueOf(entry.getValue()));
            }
            map.put("s3Metadata", new MessageHeaders(metadata));
        }
    }

    @Override
    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        try {
            return doListFiles(fromDir, filenamePattern, isRegex, ignoreDot);
        } catch (AmazonServiceException e) {
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
        AmazonS3 client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String dirPrefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        ListObjectsV2Request request = createListRequest(bucketName, dirPrefix);

        // Add the file prefix if necessary
        if (StringUtils.isNotBlank(filePrefix)) {
            request.setPrefix(StringUtils.trimToEmpty(dirPrefix) + filePrefix);
        }

        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(request);

            for (S3ObjectSummary summary : result.getObjectSummaries()) {
                // Ignore the folder itself
                if (!StringUtils.equals(summary.getKey(), dirPrefix)) {
                    S3FileInfo fileInfo = new S3FileInfo(summary);

                    // Validate against the filter before adding to the list 
                    if ((filenameFilter == null || filenameFilter.accept(null, fileInfo.getName())) && (!ignoreDot || !StringUtils.startsWith(fileInfo.getName(), "."))) {
                        fileInfoList.add(fileInfo);
                    }
                }
            }

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return fileInfoList;
    }

    @Override
    public List<String> listDirectories(String fromDir) throws Exception {
        try {
            return doListDirectories(fromDir);
        } catch (AmazonServiceException e) {
            handleException(e);
            return doListDirectories(fromDir);
        }
    }

    List<String> doListDirectories(String fromDir) throws Exception {
        List<String> directories = new ArrayList<String>();
        AmazonS3 client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        ListObjectsV2Request request = createListRequest(bucketName, prefix);
        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(request);

            for (String commonPrefix : result.getCommonPrefixes()) {
                directories.add(bucketName + DELIMITER + commonPrefix);
            }

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return directories;
    }

    @Override
    public boolean exists(String file, String path) throws Exception {
        try {
            return doExists(file, path);
        } catch (AmazonServiceException e) {
            handleException(e);
            return doExists(file, path);
        }
    }

    boolean doExists(String file, String path) throws Exception {
        AmazonS3 client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(path);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        return client.doesObjectExist(bucketName, key);
    }

    @Override
    public InputStream readFile(String file, String fromDir, Map<String, Object> sourceMap) throws Exception {
        try {
            return doReadFile(file, fromDir, sourceMap);
        } catch (AmazonServiceException e) {
            handleException(e);
            return doReadFile(file, fromDir, sourceMap);
        }
    }

    InputStream doReadFile(String file, String fromDir, Map<String, Object> sourceMap) throws Exception {
        AmazonS3 client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        GetObjectRequest request = new GetObjectRequest(bucketName, key);

        S3Object object = client.getObject(request);

        populateObjectMetadata(sourceMap, object.getObjectMetadata());

        return object.getObjectContent();
    }

    @Override
    public void closeReadFile() throws Exception {}

    @Override
    public boolean canAppend() {
        // S3 doesn't allow appending
        return false;
    }

    @Override
    public void writeFile(String file, String toDir, boolean append, InputStream message, Map<String, Object> connectorMap) throws Exception {
        try {
            doWriteFile(file, toDir, append, message, connectorMap);
        } catch (AmazonServiceException e) {
            handleException(e);
            doWriteFile(file, toDir, append, message, connectorMap);
        }
    }

    void doWriteFile(String file, String toDir, boolean append, InputStream message, Map<String, Object> connectorMap) throws Exception {
        AmazonS3 client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(toDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, message, null);
        addCustomHeaders(putRequest);

        PutObjectResult result = client.putObject(putRequest);

        if (connectorMap != null) {
            addMetadataIfNotNull(connectorMap, "s3ETag", result.getETag());
            addMetadataIfNotNull(connectorMap, "s3ExpirationTime", result.getExpirationTime());
            addMetadataIfNotNull(connectorMap, "s3ExpirationTimeRuleId", result.getExpirationTimeRuleId());
            addMetadataIfNotNull(connectorMap, "s3SSEAlgorithm", result.getSSEAlgorithm());
            addMetadataIfNotNull(connectorMap, "s3SSECustomerAlgorithm", result.getSSECustomerAlgorithm());
            addMetadataIfNotNull(connectorMap, "s3SSECustomerKeyMd5", result.getSSECustomerKeyMd5());
            addMetadataIfNotNull(connectorMap, "s3VersionId", result.getVersionId());
            populateObjectMetadata(connectorMap, result.getMetadata());
        }
    }

    @Override
    public void delete(String file, String fromDir, boolean mayNotExist) throws Exception {
        try {
            doDelete(file, fromDir, mayNotExist);
        } catch (AmazonServiceException e) {
            handleException(e);
            doDelete(file, fromDir, mayNotExist);
        }
    }

    void doDelete(String file, String fromDir, boolean mayNotExist) throws Exception {
        AmazonS3 client = getClient();

        Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(fromDir);
        String bucketName = bucketNameAndPrefix.getLeft();
        String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

        String key = file;
        if (StringUtils.isNotBlank(prefix) && !StringUtils.equals(prefix, DELIMITER)) {
            key = prefix + key;
        }

        DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, key);

        client.deleteObject(deleteRequest);

        if (mayNotExist && exists(file, fromDir)) {
            throw new FileConnectorException("File should not exist after deleting, bucket: " + fromDir + ", file: " + file);
        }
    }

    @Override
    public void move(String fromName, String fromDir, String toName, String toDir) throws Exception {
        try {
            doMove(fromName, fromDir, toName, toDir);
        } catch (AmazonServiceException e) {
            handleException(e);
            doMove(fromName, fromDir, toName, toDir);
        }
    }

    void doMove(String fromName, String fromDir, String toName, String toDir) throws Exception {
        AmazonS3 client = getClient();

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
                GetObjectRequest getRequest = new GetObjectRequest(fromBucketName, fromKey);
                S3Object object = client.getObject(getRequest);
                PutObjectRequest putRequest = new PutObjectRequest(toBucketName, toKey, object.getObjectContent(), null);
                addCustomHeaders(putRequest);
                client.putObject(putRequest);
            } else {
                CopyObjectRequest copyRequest = new CopyObjectRequest(fromBucketName, fromKey, toBucketName, toKey);
                client.copyObject(copyRequest);
            }

            // delete original
            delete(fromName, fromDir, false);
        } catch (Exception e) {
            throw new FileConnectorException("Error moving file from [bucket: " + fromBucketName + ", key: " + fromKey + "] to [bucket: " + toBucketName + ", key: " + toKey + "]", e);
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
            client.shutdown();
        }
        if (sts != null) {
            sts.shutdown();
        }
    }

    @Override
    public boolean isValid() {
        return client != null || sts != null;
    }

    @Override
    public boolean canRead(String readDir) {
        try {
            AmazonS3 client = getClient();

            Pair<String, String> bucketNameAndPrefix = getBucketNameAndPrefix(readDir);
            String bucketName = bucketNameAndPrefix.getLeft();
            String prefix = normalizeKey(bucketNameAndPrefix.getRight(), false, false);
            String prefixWithTrailingDelimiter = normalizeKey(bucketNameAndPrefix.getRight(), false, true);

            ListObjectsV2Request request = createListRequest(bucketName, prefix);
            ListObjectsV2Result result;

            do {
                result = client.listObjectsV2(request);

                if (StringUtils.isBlank(prefixWithTrailingDelimiter) || result.getCommonPrefixes().contains(prefixWithTrailingDelimiter)) {
                    return true;
                }

                request.setContinuationToken(result.getNextContinuationToken());
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

    void handleException(AmazonServiceException e) throws AmazonServiceException {
        if (isSTSEnabled() && StringUtils.equals(e.getErrorCode(), "ExpiredToken")) {
            if (client != null) {
                client.shutdown();
                client = null;
            }
        } else {
            throw e;
        }
    }
}
