package com.mirth.connect.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.util.messagewriter.MessageWriter;

public class MessageExporter {
    private int numExported;
    private int numProcessed;
    private Logger logger = Logger.getLogger(getClass());

    public int getNumExported() {
        return numExported;
    }
    
    public int getNumProcessed() {
        return numProcessed;
    }

    /**
     * Executes the message exporter.
     * 
     * @param messageList
     *            The paginated list to read messages from
     * @param messageWriter
     *            The message writer to write messages to
     * @return A list of the message ids that were exported.
     */
    public synchronized MessageExportResult exportMessages(PaginatedList<Message> messageList, MessageWriter messageWriter) throws InterruptedException, MessageExportException {
        List<Long> processedMessageIds = new ArrayList<Long>();
        int pageNumber = 0;
        numExported = 0;

        do {
            ThreadUtils.checkInterruptedStatus();

            try {
                messageList.loadPageNumber(++pageNumber);
            } catch (Exception e) {
                throw new MessageExportException(e);
            }

            for (Message message : messageList) {
                ThreadUtils.checkInterruptedStatus();

                try {
                    if (messageWriter.write(message)) {
                        numExported++;
                    }

                    numProcessed++;
                    processedMessageIds.add(message.getMessageId());
                } catch (Exception e) {
                    throw new MessageExportException("Failed to export message", e);
                }
            }
        } while (messageList.hasNextPage());

        try {
            messageWriter.close();
        } catch (Exception e) {
            logger.error("Failed to close message writer", e);
        }

        return new MessageExportResult(processedMessageIds, numExported);
    }

    public class MessageExportResult {
        private List<Long> processedIds;
        private int numExported;

        private MessageExportResult(List<Long> processedIds, int numExported) {
            this.processedIds = processedIds;
            this.numExported = numExported;
        }

        /**
         * @return A list of the message ids that were processed, regardless of whether or not
         *         exported content was produced
         */
        public List<Long> getProcessedIds() {
            return processedIds;
        }

        /**
         * @return The number of messages that actually produced exported content
         */
        public int getNumExported() {
            return numExported;
        }
    }

    public static class MessageExportException extends Exception {
        public MessageExportException(String message) {
            super(message);
        }

        public MessageExportException(Throwable cause) {
            super(cause);
        }

        public MessageExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
