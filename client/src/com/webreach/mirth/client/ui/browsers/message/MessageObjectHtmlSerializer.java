package com.webreach.mirth.client.ui.browsers.message;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.MessageObject;

public class MessageObjectHtmlSerializer {

    private final String MESSAGE_ID_COLUMN_NAME = "Message ID";
    private final String DATE_COLUMN_NAME = "Date";
    private final String CONNECTOR_COLUMN_NAME = "Connector";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String RAW_COLUMN_NAME = "Raw Message";
    private final String ENCODED_COLUMN_NAME = "Encoded Message";
    private final String TRANSFORMED_COLUMN_NAME = "Transformed Message";
    private String[] statsColumns = {MESSAGE_ID_COLUMN_NAME, DATE_COLUMN_NAME, CONNECTOR_COLUMN_NAME, STATUS_COLUMN_NAME};
    private String[] dataColumns = {MESSAGE_ID_COLUMN_NAME, RAW_COLUMN_NAME, ENCODED_COLUMN_NAME, TRANSFORMED_COLUMN_NAME};

    /** Creates a new instance of MessageObjectHtmlSerializer */
    public MessageObjectHtmlSerializer() {
    }

    public StringBuffer toHtml(List<MessageObject> messages) {
        StringBuffer results = new StringBuffer();
        results.append("<html>\n");
        results.append("<head>\n");
        results.append("<title>Messages</title>\n");
        results.append("</head>");
        results.append("<body>");

        if (messages.size() > 0) {
            results.append("<h><b>Messages</b></h>\n");
            results.append("<br><br>\n");
            results.append("<table border=\"1\" cellpadding=\"10\">\n");
            results.append("<tr>");
            for (int i = 0; i < statsColumns.length; i++) {
                results.append("<td><b>" + statsColumns[i] + "</b></td>");
            }
            results.append("</tr>");

            for (int i = 0; i < messages.size(); i++) {
                results.append(getMessageStats(messages.get(i)));
                results.append("\n");
            }
            results.append("</table>");
        } else {
            results.append("<table border=\"1\" cellpadding=\"10\">");
            results.append("<tr><td>No results found.</td></tr></table>");
        }
        results.append("</body>");
        results.append("</html>");
        return results;
    }

    public String getMessageStats(MessageObject message) {
        String results = "";
        results += "<tr>";

        results += "<td>" + message.getId() + "</td>";
        results += "<td>" + String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", message.getDateCreated()) + "</td>";
        results += "<td>" + message.getConnectorName() + "</td>";
        results += " <td>" + message.getStatus().name() + "</td>";

        results += "</tr>";
        return results;
    }

    public void outputMessages(List<MessageObject> messages, File rawFile, File transformedFile, File encodedFile) throws IOException {
        String rawData = "";
        String transformedData = "";
        String encodedData = "";

        for (int i = 0; i < messages.size(); i++) {
            MessageObject current = messages.get(i);
            rawData += "Message ID: " + current.getId();
            rawData += "\n\n";
            rawData += current.getRawData();
            rawData += "\n\n";

            transformedData += "Message ID: " + current.getId();
            transformedData += "\n\n";
            if (current.getTransformedData() != null) {
                transformedData += current.getTransformedData();
            } else {
                transformedData += "No transformed message.";
            }
            transformedData += "\n\n";

            encodedData += "Message ID: " + current.getId();
            encodedData += "\n\n";
            if (current.getEncodedData() != null) {
                encodedData += current.getEncodedData();
            } else {
                encodedData += "No encoded message.";
            }
            encodedData += "\n\n";
        }

        FileUtil.write(rawFile, rawData, false);
        FileUtil.write(transformedFile, transformedData, false);
        FileUtil.write(encodedFile, encodedData, false);
    }
}
