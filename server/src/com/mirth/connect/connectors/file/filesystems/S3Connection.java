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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.SimpleMaterialProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.mirth.connect.connectors.file.FileConnectorException;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.S3SchemeProperties;
import com.mirth.connect.connectors.file.filters.RegexFilenameFilter;

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
    }

    private Logger logger = Logger.getLogger(getClass());
    private String bucketName;
    private FileSystemConnectionOptions fileSystemOptions;
    private S3SchemeProperties schemeProps;
    private AmazonS3EncryptionClientBuilder clientBuilder;
    private AmazonS3 client;
    private AWSSecurityTokenService sts;
    private int stsDuration;

    public S3Connection(String bucketName, FileSystemConnectionOptions fileSystemOptions, int timeout) throws Exception {
        this.bucketName = bucketName;
        this.fileSystemOptions = fileSystemOptions;
        schemeProps = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

        clientBuilder = new AmazonS3EncryptionClientBuilder();
        clientBuilder.setClientConfiguration(createClientConfiguration(timeout));

        if (schemeProps.isUseTemporaryCredentials()) {
            AWSSecurityTokenServiceClientBuilder stsClientBuilder = AWSSecurityTokenServiceClientBuilder.standard();
            stsClientBuilder.setClientConfiguration(createClientConfiguration(timeout));
            stsClientBuilder.setCredentials(createCredentialsProvider(fileSystemOptions));
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
            clientBuilder.setEncryptionMaterials(new SimpleMaterialProvider());
            client = clientBuilder.build();
        }
    }

    ClientConfiguration createClientConfiguration(int timeout) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(timeout);
        clientConfiguration.setSocketTimeout(timeout);
        return clientConfiguration;
    }

    AWSCredentialsProvider createCredentialsProvider(FileSystemConnectionOptions fileSystemOptions) {
        S3SchemeProperties schemeProps = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

        if (StringUtils.isNotBlank(fileSystemOptions.getUsername()) && StringUtils.isNotBlank(fileSystemOptions.getPassword())) {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(fileSystemOptions.getUsername(), fileSystemOptions.getPassword()));
        } else if (schemeProps.isUseDefaultCredentialProviderChain()) {
            return new DefaultAWSCredentialsProviderChain();
        } else {
            return new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        }
    }

    AmazonS3 getClient() {
        if (schemeProps.isUseTemporaryCredentials() && client == null) {
            GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest();
            getSessionTokenRequest.setDurationSeconds(stsDuration);

            GetSessionTokenResult sessionTokenResult = sts.getSessionToken(getSessionTokenRequest);
            Credentials sessionCredentials = sessionTokenResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(), sessionCredentials.getSessionToken());
            clientBuilder.setCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials));
            clientBuilder.setEncryptionMaterials(new SimpleMaterialProvider());
            client = clientBuilder.build();
        }

        return client;
    }

    String getPrefix(String fromDir, boolean trailingDelimiter) {
        String prefix = null;
        if (StringUtils.isNotBlank(fromDir) && !StringUtils.equals(fromDir, DELIMITER)) {
            prefix = fromDir;
            while (StringUtils.startsWith(prefix, DELIMITER)) {
                prefix = prefix.substring(1);
            }
            if (trailingDelimiter && !StringUtils.endsWith(prefix, DELIMITER)) {
                prefix += DELIMITER;
            }
        }
        return prefix;
    }

    ListObjectsV2Request createListRequest(String fromDir, boolean trailingPrefixDelimiter) {
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(getPrefix(fromDir, trailingPrefixDelimiter)).withDelimiter(DELIMITER);
        addCustomHeaders(request);
        return request;
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

    @Override
    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
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

        ListObjectsV2Request request = createListRequest(fromDir, true);
        String dirPrefix = request.getPrefix();

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
        List<String> directories = new ArrayList<String>();
        AmazonS3 client = getClient();

        ListObjectsV2Request request = createListRequest(fromDir, true);
        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(request);

            directories.addAll(result.getCommonPrefixes());

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return directories;
    }

    @Override
    public boolean exists(String file, String path) throws Exception{
        AmazonS3 client = getClient();
        
        return client.doesObjectExist(path, file);
    }

    @Override
    public InputStream readFile(String file, String fromDir) throws Exception {
        AmazonS3 client = getClient();

        if (StringUtils.equals(fromDir, bucketName)) {
            fromDir = "";
        } else {
            fromDir = StringUtils.removeStart(fromDir, bucketName + DELIMITER);
        }

        String key = file;
        if (StringUtils.isNotBlank(fromDir) && !StringUtils.equals(fromDir, DELIMITER)) {
            key = fromDir + DELIMITER + key;
        }

        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        addCustomHeaders(request);

        S3Object object = client.getObject(request);
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
    public void writeFile(String file, String toDir, boolean append, InputStream message) throws Exception {
        AmazonS3 client = getClient();
        
        // TODO Figure out if ObjectMetadata is necessary
        ObjectMetadata objectMetadata = new ObjectMetadata();
        PutObjectRequest objectRequest = new PutObjectRequest(toDir, file, message, null);
        client.putObject(objectRequest);
    }

    @Override
    public void delete(String file, String fromDir, boolean mayNotExist) throws Exception {
        AmazonS3 client = getClient();
        
        client.deleteObject(fromDir, file);
        
        if (mayNotExist && exists(file, fromDir)) {
            throw new FileConnectorException("File should not exist after deleting, bucket: " + fromDir + ", file: " + file);
        }
    }

    @Override
    public void move(String fromName, String fromDir, String toName, String toDir) throws Exception {
        AmazonS3 client =  getClient();
        
        try {
            // copy to new bucket
            client.copyObject(fromDir, fromName, toDir, toName);
            
            // delete original
            delete(fromName, fromDir, false);
        } catch (Exception e) {
            throw new FileConnectorException("Error moving file from [bucket: " + fromDir + ", file: " + fromName + "] to [bucket: " + toDir + ", file: " + toName + "]", e);
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
            String prefix = getPrefix(readDir, true);
            ListObjectsV2Request request = createListRequest(readDir, false);
            ListObjectsV2Result result;

            do {
                result = client.listObjectsV2(request);

                if (StringUtils.isBlank(prefix) || result.getCommonPrefixes().contains(prefix)) {
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
        // TODO Auto-generated method stub
        return false;
    }

}
