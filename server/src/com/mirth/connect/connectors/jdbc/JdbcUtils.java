package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.StringUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class JdbcUtils {
    /**
     * Parse the given statement filling the parameter list and return the ready to use statement.
     * 
     * @param statement
     * @param params
     *            List that will contain the parameters found in the statement
     * @return The parsed statement
     */
    public static String extractParameters(String statement, List<String> params) {
        if (statement == null) {
            return null;
        }

        Pattern p = Pattern.compile("\\$\\{([^\\}]*)\\}");
        Matcher m = p.matcher(statement);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String key = m.group(0);
            m.appendReplacement(sb, "?");
            params.add(key);
        }

        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Tests if the given connection is valid and not closed
     */
    public static boolean isValidConnection(Connection connection) {
        /*
         * Check if the connection is still valid. JDBC driver
         * throws an unexpected error when calling isValid for some
         * drivers (i.e. informix), so assume the connection is not
         * valid if an exception occurs
         */
        try {
            return connection.isValid(10000);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Get an array of parameter values using a TemplateValueReplacer based on the given list of
     * parameter keys, using a ConnectorMessage and/or Map&lt;String, Object&gt; to look up the
     * values.
     * 
     * @param paramNames
     *            A list of the parameter names to look up
     * @param channelId
     *            A channel id to use for the default context in the TemplateValueReplacer
     * @param connectorMessage
     *            A connector message to reference when looking up values
     * @param map
     *            A String/Object map to reference when looking up values
     * @return
     */
    public static Object[] getParameters(List<String> paramNames, String channelId, ConnectorMessage connectorMessage, Map<String, Object> map) {
        Object[] params = new Object[paramNames.size()];
        TemplateValueReplacer replacer = new TemplateValueReplacer();
        int i = 0;

        for (String paramName : paramNames) {
            String key = paramName.substring(2, paramName.length() - 1);
            Object value;

            if (map != null && map.containsKey(key)) {
                value = map.get(key);
            } else if (connectorMessage != null) {
                value = replacer.replaceValues(paramName, connectorMessage);
            } else {
                value = replacer.replaceValues(paramName, channelId);
            }

            if (AttachmentUtil.hasAttachmentKeys(value.toString())) {
                value = StringUtils.newString(AttachmentUtil.reAttachMessage(value.toString(), connectorMessage, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET);
            }

            params[i++] = value;
        }

        return params;
    }
}
