/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.util.CharsetUtils;

public class FileReceiverProperties extends ConnectorProperties implements PollConnectorPropertiesInterface, SourceConnectorPropertiesInterface {

    public static final String NAME = "File Reader";

    private PollConnectorProperties pollConnectorProperties;
    private SourceConnectorProperties sourceConnectorProperties;

    private FileScheme scheme;
    private String host;
    private String fileFilter;
    private boolean regex;
    private boolean directoryRecursion;
    private boolean ignoreDot;
    private boolean anonymous;
    private String username;
    private String password;
    private String timeout;
    private boolean secure;
    private boolean passive;
    private boolean validateConnection;
    private FileAction afterProcessingAction;
    private String moveToDirectory;
    private String moveToFileName;
    private FileAction errorReadingAction;
    private FileAction errorResponseAction;
    private String errorMoveToDirectory;
    private String errorMoveToFileName;
    private boolean checkFileAge;
    private String fileAge;
    private String fileSizeMinimum;
    private String fileSizeMaximum;
    private boolean ignoreFileSizeMaximum;
    private String sortBy;
    private boolean binary;
    private String charsetEncoding;

    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_SIZE = "size";
    public static final String SORT_BY_DATE = "date";

    public FileReceiverProperties() {
        pollConnectorProperties = new PollConnectorProperties();
        sourceConnectorProperties = new SourceConnectorProperties();

        scheme = FileScheme.FILE;
        host = "";
        fileFilter = "*";
        regex = false;
        directoryRecursion = false;
        ignoreDot = true;
        anonymous = true;
        username = "anonymous";
        password = "anonymous";
        timeout = "10000";
        secure = true;
        passive = true;
        validateConnection = true;
        afterProcessingAction = FileAction.NONE;
        moveToDirectory = "";
        moveToFileName = "";
        errorReadingAction = FileAction.NONE;
        errorResponseAction = FileAction.AFTER_PROCESSING;
        errorMoveToDirectory = "";
        errorMoveToFileName = "";
        checkFileAge = true;
        fileAge = "1000";
        fileSizeMinimum = "0";
        fileSizeMaximum = "";
        ignoreFileSizeMaximum = true;
        sortBy = SORT_BY_DATE;
        binary = false;
        charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
    }

    public FileScheme getScheme() {
        return scheme;
    }

    public void setScheme(FileScheme scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(String fileFilter) {
        this.fileFilter = fileFilter;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public boolean isDirectoryRecursion() {
        return directoryRecursion;
    }

    public void setDirectoryRecursion(boolean directoryRecursion) {
        this.directoryRecursion = directoryRecursion;
    }

    public boolean isIgnoreDot() {
        return ignoreDot;
    }

    public void setIgnoreDot(boolean ignoreDot) {
        this.ignoreDot = ignoreDot;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public boolean isValidateConnection() {
        return validateConnection;
    }

    public void setValidateConnection(boolean validateConnection) {
        this.validateConnection = validateConnection;
    }

    public FileAction getAfterProcessingAction() {
        return afterProcessingAction;
    }

    public void setAfterProcessingAction(FileAction afterProcessingAction) {
        this.afterProcessingAction = afterProcessingAction;
    }

    public String getMoveToDirectory() {
        return moveToDirectory;
    }

    public void setMoveToDirectory(String moveToDirectory) {
        this.moveToDirectory = moveToDirectory;
    }

    public String getMoveToFileName() {
        return moveToFileName;
    }

    public void setMoveToFileName(String moveToFileName) {
        this.moveToFileName = moveToFileName;
    }

    public FileAction getErrorReadingAction() {
        return errorReadingAction;
    }

    public void setErrorReadingAction(FileAction errorReadingAction) {
        this.errorReadingAction = errorReadingAction;
    }

    public FileAction getErrorResponseAction() {
        return errorResponseAction;
    }

    public void setErrorResponseAction(FileAction errorResponseAction) {
        this.errorResponseAction = errorResponseAction;
    }

    public String getErrorMoveToDirectory() {
        return errorMoveToDirectory;
    }

    public void setErrorMoveToDirectory(String errorMoveToDirectory) {
        this.errorMoveToDirectory = errorMoveToDirectory;
    }

    public String getErrorMoveToFileName() {
        return errorMoveToFileName;
    }

    public void setErrorMoveToFileName(String errorMoveToFileName) {
        this.errorMoveToFileName = errorMoveToFileName;
    }

    public boolean isCheckFileAge() {
        return checkFileAge;
    }

    public void setCheckFileAge(boolean checkFileAge) {
        this.checkFileAge = checkFileAge;
    }

    public String getFileAge() {
        return fileAge;
    }

    public void setFileAge(String fileAge) {
        this.fileAge = fileAge;
    }

    public String getFileSizeMinimum() {
        return fileSizeMinimum;
    }

    public void setFileSizeMinimum(String fileSizeMinimum) {
        this.fileSizeMinimum = fileSizeMinimum;
    }

    public String getFileSizeMaximum() {
        return fileSizeMaximum;
    }

    public void setFileSizeMaximum(String fileSizeMaximum) {
        this.fileSizeMaximum = fileSizeMaximum;
    }

    public boolean isIgnoreFileSizeMaximum() {
        return ignoreFileSizeMaximum;
    }

    public void setIgnoreFileSizeMaximum(boolean ignoreFileSizeMaximum) {
        this.ignoreFileSizeMaximum = ignoreFileSizeMaximum;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    @Override
    public String getProtocol() {
        return "File";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public PollConnectorProperties getPollConnectorProperties() {
        return pollConnectorProperties;
    }

    @Override
    public SourceConnectorProperties getSourceConnectorProperties() {
        return sourceConnectorProperties;
    }

    @Override
    public boolean canBatch() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        String processBatch = element.removeChild("processBatch").getTextContent();
        DonkeyElement sourcePropertiesElement = element.getChildElement("sourceConnectorProperties");
        if (sourcePropertiesElement != null) {
            sourcePropertiesElement.addChildElement("processBatch", processBatch);
        }
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("pollConnectorProperties", pollConnectorProperties.getPurgedProperties());
        purgedProperties.put("resposeConnectorProperties", sourceConnectorProperties.getPurgedProperties());
        purgedProperties.put("scheme", scheme);
        purgedProperties.put("regex", regex);
        purgedProperties.put("directoryRecursion", directoryRecursion);
        purgedProperties.put("ignoreDot", ignoreDot);
        purgedProperties.put("anonymous", anonymous);
        purgedProperties.put("timeout", PurgeUtil.getNumericValue(timeout));
        purgedProperties.put("secure", secure);
        purgedProperties.put("passive", passive);
        purgedProperties.put("validateConnection", validateConnection);
        purgedProperties.put("afterProcessingAction", afterProcessingAction);
        purgedProperties.put("errorReadingAction", errorReadingAction);
        purgedProperties.put("checkFileAge", checkFileAge);
        purgedProperties.put("fileAge", PurgeUtil.getNumericValue(fileAge));
        purgedProperties.put("fileSizeMinimum", PurgeUtil.getNumericValue(fileSizeMinimum));
        purgedProperties.put("fileSizeMaximum", PurgeUtil.getNumericValue(fileSizeMaximum));
        purgedProperties.put("ignoreFileSizeMaximum", ignoreFileSizeMaximum);
        purgedProperties.put("sortBy", sortBy);
        return purgedProperties;
    }
}
