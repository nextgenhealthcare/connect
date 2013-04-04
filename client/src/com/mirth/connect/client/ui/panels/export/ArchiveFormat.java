/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.export;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public enum ArchiveFormat {
    // @formatter:off
    ZIP(ArchiveStreamFactory.ZIP, null),
    TAR_GZ(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP),
    TAR_BZ2(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2);
    // @formatter:on

    private String archiver;
    private String compressor;
    private String label;

    private ArchiveFormat(String archiver, String compressor) {
        this.archiver = archiver;
        this.compressor = compressor;

        if (compressor == null) {
            label = archiver;
        } else {
            if (compressor.equals(CompressorStreamFactory.BZIP2)) {
                compressor = "bz2";
            }

            label = archiver + "." + compressor;
        }
    }

    public String getArchiver() {
        return archiver;
    }

    public String getCompressor() {
        return compressor;
    }

    public String toString() {
        return label;
    }

    public static ArchiveFormat lookup(String archiver, String compressor) {
        for (ArchiveFormat archiveFormat : ArchiveFormat.values()) {
            if (archiveFormat.getArchiver().equals(archiver) && ((compressor == null && archiveFormat.getCompressor() == null) || archiveFormat.getCompressor().equals(compressor))) {
                return archiveFormat;
            }
        }

        return null;
    }
}
