/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

import com.mirth.connect.donkey.util.ByteCounterOutputStream;

public class DICOMConverter {

    public static DicomObject byteArrayToDicomObject(byte[] bytes, boolean decodeBase64) throws IOException {
        DicomObject basicDicomObject = new BasicDicomObject();
        DicomInputStream dis = null;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            InputStream inputStream;
            if (decodeBase64) {
                inputStream = new BufferedInputStream(new Base64InputStream(bais));
            } else {
                inputStream = bais;
            }
            dis = new DicomInputStream(inputStream);
            /*
             * This parameter was added in dcm4che 2.0.28. We use it to retain the memory allocation
             * behavior from 2.0.25.
             * http://www.mirthcorp.com/community/issues/browse/MIRTH-2166
             * http://www.dcm4che.org/jira/browse/DCM-554
             */
            dis.setAllocateLimit(-1);
            dis.readDicomObject(basicDicomObject, -1);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(dis);
        }

        return basicDicomObject;
    }

    public static byte[] dicomObjectToByteArray(DicomObject dicomObject) throws IOException {
        BasicDicomObject basicDicomObject = (BasicDicomObject) dicomObject;
        DicomOutputStream dos = null;

        try {
            ByteCounterOutputStream bcos = new ByteCounterOutputStream();
            ByteArrayOutputStream baos;

            if (basicDicomObject.fileMetaInfo().isEmpty()) {
                try {
                    // Create a dicom output stream with the byte counter output stream.
                    dos = new DicomOutputStream(bcos);
                    // "Write" the dataset once to determine the total number of bytes required. This is fast because no data is actually being copied.
                    dos.writeDataset(basicDicomObject, TransferSyntax.ImplicitVRLittleEndian);
                } finally {
                    IOUtils.closeQuietly(dos);
                }

                // Create the actual byte array output stream with a buffer size equal to the number of bytes required.
                baos = new ByteArrayOutputStream(bcos.size());
                // Create a dicom output stream with the byte array output stream
                dos = new DicomOutputStream(baos);

                // Create ACR/NEMA Dump
                dos.writeDataset(basicDicomObject, TransferSyntax.ImplicitVRLittleEndian);
            } else {
                try {
                    // Create a dicom output stream with the byte counter output stream.
                    dos = new DicomOutputStream(bcos);
                    // "Write" the dataset once to determine the total number of bytes required. This is fast because no data is actually being copied.
                    dos.writeDicomFile(basicDicomObject);
                } finally {
                    IOUtils.closeQuietly(dos);
                }

                // Create the actual byte array output stream with a buffer size equal to the number of bytes required.
                baos = new ByteArrayOutputStream(bcos.size());
                // Create a dicom output stream with the byte array output stream
                dos = new DicomOutputStream(baos);

                // Create DICOM File
                dos.writeDicomFile(basicDicomObject);
            }

            // Memory Optimization since the dicom object is no longer needed at this point.
            dicomObject.clear();

            return baos.toByteArray();
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(dos);
        }
    }
}
