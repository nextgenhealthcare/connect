/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.Base64Util;

public class AttachmentUtil {

    public static void writeToFile(String filePath, Attachment attachment, boolean binary) throws IOException {
        File file = new File(filePath);
        if (!file.canWrite()) {
            String dirName = file.getPath();
            int i = dirName.lastIndexOf(File.separator);
            if (i > -1) {
                dirName = dirName.substring(0, i);
                File dir = new File(dirName);
                dir.mkdirs();
            }
            file.createNewFile();
        }

        if (attachment != null && StringUtils.isNotEmpty(filePath)) {
            FileUtils.writeByteArrayToFile(file, binary ? Base64Util.decodeBase64(attachment.getContent()) : attachment.getContent());
        }
    }

    public static void decodeBase64(List<Attachment> attachments) {
        for (Attachment attachment : attachments) {
            decodeBase64(attachment);
        }
    }

    public static void decodeBase64(Attachment attachment) {
        try {
            attachment.setContent(Base64Util.decodeBase64(attachment.getContent()));
        } catch (IOException e) {
        }
    }
}