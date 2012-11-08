package com.mirth.connect.plugins.messagepruner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.server.controllers.MessagePrunerException;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultMessagePruner implements MessagePruner {
    private List<Status> skipStatuses = new ArrayList<Status>();
    private boolean skipIncomplete = true;
    private int retryCount = 0;
    private MessageArchiver messageArchiver;

    public List<Status> getSkipStatuses() {
        return skipStatuses;
    }

    public void setSkipStatuses(List<Status> skipStatuses) {
        this.skipStatuses = skipStatuses;
    }

    public boolean isSkipIncomplete() {
        return skipIncomplete;
    }

    public void setSkipIncomplete(boolean skipIncomplete) {
        this.skipIncomplete = skipIncomplete;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public MessageArchiver getMessageArchiver() {
        return messageArchiver;
    }

    public void setMessageArchiver(MessageArchiver archiver) {
        this.messageArchiver = archiver;
    }

    @Override
    public int executePruner(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws MessagePrunerException {
        if (messageDateThreshold == null && contentDateThreshold == null) {
            return 0;
        }

        if (messageDateThreshold != null && contentDateThreshold != null && contentDateThreshold.getTimeInMillis() <= messageDateThreshold.getTimeInMillis()) {
            contentDateThreshold = null;
        }

        int tryNum = 1;
        int numPruned = 0;
        boolean retry;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("skipIncomplete", skipIncomplete);

        if (!skipStatuses.isEmpty()) {
            params.put("skipStatuses", skipStatuses);
        }

        do {
            SqlSession session = SqlConfig.getSqlSessionManager().openSession();
            retry = false;

            try {
                if (messageArchiver != null) {
                    params.put("dateThreshold", (contentDateThreshold != null) ? contentDateThreshold : messageDateThreshold);
                    DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

                    try {
                        session.select("prunerSelectMessagesToArchive", params, new ArchiverResultHandler(dao, channelId));
                    } finally {
                        dao.close();
                    }
                }

                // if either delete query fails, it is possible that some messages will have been archived, but not yet pruned
                if (contentDateThreshold != null) {
                    params.put("dateThreshold", contentDateThreshold);
                    session.delete("prunerDeleteMessageContent", params);
                }

                if (messageDateThreshold != null) {
                    params.put("dateThreshold", messageDateThreshold);
                    numPruned += session.delete("prunerDeleteMessages", params);
                }

                session.commit();
            } catch (Exception e) {
                retry = true;

                if (tryNum > retryCount) {
                    throw new MessagePrunerException("Failed to prune messages", e);
                } else {
                    tryNum++;
                }
            } finally {
                session.close();
            }
        } while (retry);

        return numPruned;
    }

    private class ArchiverResultHandler implements ResultHandler {
        private DonkeyDao dao;
        private String channelId;

        public ArchiverResultHandler(DonkeyDao dao, String channelId) {
            this.dao = dao;
            this.channelId = channelId;
        }

        @Override
        public void handleResult(ResultContext context) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) context.getResultObject();

            Long messageId = (Long) result.get("id");

            if (!messageArchiver.isArchived(messageId)) {
                Calendar dateCreated = Calendar.getInstance();
                dateCreated.setTimeInMillis(((Timestamp) result.get("date_created")).getTime());

                Message message = new Message();
                message.setMessageId(messageId);
                message.setChannelId(channelId);
                message.setDateCreated(dateCreated);
                message.setProcessed((Boolean) result.get("processed"));
                message.setServerId((String) result.get("server_id"));
                message.getConnectorMessages().putAll(dao.getConnectorMessages(channelId, messageId));

                messageArchiver.archiveMessage(message);
            }
        }
    }
}
