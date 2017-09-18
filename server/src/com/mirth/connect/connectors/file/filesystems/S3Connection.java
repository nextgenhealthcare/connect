/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.mirth.connect.connectors.file.FileConnectorException;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.S3SchemeProperties;

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

    private FileSystemConnectionOptions fileSystemOptions;
    private S3SchemeProperties schemeProps;
    private AmazonS3EncryptionClientBuilder clientBuilder;
    private AmazonS3 client;
    private AWSSecurityTokenService sts;
    private int stsDuration;

    public S3Connection(FileSystemConnectionOptions fileSystemOptions, int timeout) throws Exception {
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

        if (schemeProps.isUseDefaultCredentialProviderChain()) {
            return new DefaultAWSCredentialsProviderChain();
        } else if (StringUtils.isNotBlank(fileSystemOptions.getUsername()) && StringUtils.isNotBlank(fileSystemOptions.getPassword())) {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(fileSystemOptions.getUsername(), fileSystemOptions.getPassword()));
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
            client = clientBuilder.build();
        }
        
        return client;
    }
    
    ListObjectsV2Request createListRequest(String fromDir) {
        String bucketName = fromDir;
        String prefix = null;
        int index = fromDir.indexOf(DELIMITER);
        if (index > 0) {
            bucketName = fromDir.substring(0, index);
            prefix = fromDir.substring(index + DELIMITER.length());
        }
        
        return new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix).withDelimiter(DELIMITER);
    }

    @Override
    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        AmazonS3 client = getClient();
        
        ListObjectsV2Request request = createListRequest(fromDir);
        ListObjectsV2Result result;
        
        do {
            result = client.listObjectsV2(request);
            
            for (S3ObjectSummary summary : result.getObjectSummaries()) {
                fileInfoList.add(new S3FileInfo(summary));
            }
            
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        
        return fileInfoList;
    }

    @Override
    public List<String> listDirectories(String fromDir) throws Exception {
        List<String> directories = new ArrayList<String>();
        AmazonS3 client = getClient();
        
        ListObjectsV2Request request = createListRequest(fromDir);
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
        
        GetObjectRequest objectRequest = new GetObjectRequest(fromDir, file);
        S3Object fileObject = client.getObject(objectRequest);
        S3ObjectInputStream objectInputStream = fileObject.getObjectContent();
        
        return objectInputStream;
    }

    @Override
    public void closeReadFile() throws Exception {
        // nothing
    }

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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void activate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void passivate() {
        // TODO Auto-generated method stub

    }

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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canRead(String readDir) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canWrite(String writeDir) {
        // TODO Auto-generated method stub
        return false;
    }

}
