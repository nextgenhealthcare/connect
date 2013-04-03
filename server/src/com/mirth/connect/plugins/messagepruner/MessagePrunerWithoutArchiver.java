package com.mirth.connect.plugins.messagepruner;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.server.util.SqlConfig;

public class MessagePrunerWithoutArchiver extends MessagePruner {
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    protected int[] prune(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) {
        int numMessagesPruned = 0;
        int numContentPruned = 0;

        Integer limit = getBlockSize();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("dateThreshold", contentDateThreshold);
        params.put("skipIncomplete", isSkipIncomplete());

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        if (limit != null) {
            params.put("limit", limit);
        }

        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        long startTime = System.currentTimeMillis();

        try {
            if (contentDateThreshold != null) {
                logger.debug("Pruning content");
                numContentPruned += runDelete(session, "Message.prunerDeleteMessageContent", params, limit);
            }

            if (messageDateThreshold != null) {
                logger.debug("Pruning messages");
                params.put("dateThreshold", messageDateThreshold);

                if (contentDateThreshold == null) {
                    numContentPruned += runDelete(session, "Message.prunerDeleteMessageContent", params, limit);
                }

                runDelete(session, "Message.prunerDeleteCustomMetadata", params, limit);
                runDelete(session, "Message.prunerDeleteAttachments", params, limit);
                runDelete(session, "Message.prunerDeleteConnectorMessages", params, limit);
                numMessagesPruned += runDelete(session, "Message.prunerDeleteMessages", params, limit);
            }

            session.commit();
            logger.debug("Pruning completed in " + (System.currentTimeMillis() - startTime) + "ms");
            return new int[] { numMessagesPruned, numContentPruned };
        } finally {
            session.close();
        }
    }
}
