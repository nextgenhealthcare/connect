package com.mirth.connect.util.messagewriter;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.util.ArchiveUtils;
import com.mirth.connect.util.ArchiveUtils.CompressException;

public class MessageWriterArchive implements MessageWriter {
    private MessageWriterVfs vfsWriter;
    private File rootFolder;
    private File archiveFile;
    private String archiver;
    private String compressor;
    private boolean messagesWritten;

    /**
     * Writes messages to the file-system using MessageWriterVfs, then moves the resulting
     * folders/files into the archive file.
     * 
     * @param vfsWriter
     * @param rootFolder
     * @param archiveFile
     * @param archiver
     *            The archiver type, see org.apache.commons.compress.archivers.ArchiveStreamFactory
     * @param compressor
     *            The compressor type, see
     *            org.apache.commons.compress.compressors.CompressorStreamFactory
     */
    public MessageWriterArchive(MessageWriterVfs vfsWriter, File rootFolder, File archiveFile, String archiver, String compressor) {
        this.vfsWriter = vfsWriter;
        this.rootFolder = rootFolder;
        this.archiveFile = archiveFile;
        this.archiver = archiver;
        this.compressor = compressor;
    }

    @Override
    public boolean write(Message message) throws MessageWriterException {
        boolean result = vfsWriter.write(message);

        if (!messagesWritten && result) {
            messagesWritten = true;
        }

        return result;
    }

    /**
     * Ends message writing and moves the written folders/files into the archive file.
     */
    @Override
    public void close() throws MessageWriterException {
        vfsWriter.close();

        if (messagesWritten) {
            try {
                ArchiveUtils.createArchive(rootFolder, archiveFile, archiver, compressor);
            } catch (CompressException e) {
                throw new MessageWriterException(e);
            } finally {
                FileUtils.deleteQuietly(rootFolder);
            }
        }
    }
}
