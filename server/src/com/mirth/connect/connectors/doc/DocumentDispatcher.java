/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xml.sax.InputSource;

import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.rtf.RtfWriter2;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DocumentDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private DocumentDispatcherProperties connectorProperties;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private static long ownerPasswordSeq = System.currentTimeMillis();

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (DocumentDispatcherProperties) getConnectorProperties();

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {}

    @Override
    public void onStop() throws ConnectorTaskException {}

    @Override
    public void onHalt() throws ConnectorTaskException {}

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        DocumentDispatcherProperties documentDispatcherProperties = (DocumentDispatcherProperties) connectorProperties;

        documentDispatcherProperties.setHost(replacer.replaceValues(documentDispatcherProperties.getHost(), connectorMessage));
        documentDispatcherProperties.setOutputPattern(replacer.replaceValues(documentDispatcherProperties.getOutputPattern(), connectorMessage));
        documentDispatcherProperties.setPassword(replacer.replaceValues(documentDispatcherProperties.getPassword(), connectorMessage));
        documentDispatcherProperties.setTemplate(replacer.replaceValues(documentDispatcherProperties.getTemplate(), connectorMessage));
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        DocumentDispatcherProperties documentDispatcherProperties = (DocumentDispatcherProperties) connectorProperties;
        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED;

        String info = "";
        if (documentDispatcherProperties.isEncrypt()) {
            info = "Encrypted ";
        }
        info += documentDispatcherProperties.getDocumentType() + " Document Type Result Written To: " + documentDispatcherProperties.getHost() + "/" + documentDispatcherProperties.getOutputPattern();

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.WRITING, info));

        try {
            responseData = writeDocument(documentDispatcherProperties.getTemplate(), documentDispatcherProperties, connectorMessage);

            StringBuilder builder = new StringBuilder();
            builder.append("Document successfully written to ");

            if (StringUtils.isNotBlank(documentDispatcherProperties.getOutput())) {
                if (documentDispatcherProperties.getOutput().equalsIgnoreCase("file")) {
                    builder.append("file: ");
                    builder.append(documentDispatcherProperties.toURIString());
                } else if (documentDispatcherProperties.getOutput().equalsIgnoreCase("attachment")) {
                    builder.append("attachment");
                } else if (documentDispatcherProperties.getOutput().equalsIgnoreCase("both")) {
                    builder.append("attachment and file: ");
                    builder.append(documentDispatcherProperties.toURIString());
                }
            } else {
                builder.append("file: ");
                builder.append(documentDispatcherProperties.toURIString());
            }
            responseStatusMessage = builder.toString();

            responseStatus = Status.SENT;
        } catch (Exception e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error writing document", e));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error writing document", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error writing document", e);

            // TODO: Handle exception
//            connector.handleException(e);
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }

    private String writeDocument(String template, DocumentDispatcherProperties documentDispatcherProperties, ConnectorMessage connectorMessage) throws Exception {
        // add tags to the template to create a valid HTML document
        StringBuilder contents = new StringBuilder();
        if (template.lastIndexOf("<html") < 0) {
            contents.append("<html>");
            if (template.lastIndexOf("<body") < 0) {
                contents.append("<body>");
                contents.append(template);
                contents.append("</body>");
            } else {
                contents.append(template);
            }
            contents.append("</html>");
        } else {
            contents.append(template);
        }

        String stringContents = getAttachmentHandler().reAttachMessage(contents.toString(), connectorMessage);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (documentDispatcherProperties.getDocumentType().toLowerCase().equals("pdf")) {
            createPDF(new StringReader(stringContents), outputStream);

            boolean encrypt = documentDispatcherProperties.isEncrypt();
            String password = documentDispatcherProperties.getPassword();

            if (encrypt && password != null) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                outputStream = new ByteArrayOutputStream();
                encryptPDF(inputStream, outputStream, password);
            }
        } else if (documentDispatcherProperties.getDocumentType().toLowerCase().equals("rtf")) {
            createRTF(new ByteArrayInputStream(stringContents.getBytes()), outputStream);
        }

        if (StringUtils.isBlank(documentDispatcherProperties.getOutput()) || !documentDispatcherProperties.getOutput().equalsIgnoreCase("attachment")) {
            FileOutputStream fileOutputStream = null;
            try {
                File file = createFile(documentDispatcherProperties.getHost() + "/" + documentDispatcherProperties.getOutputPattern());
                logger.info("Writing document to: " + file.getAbsolutePath());

                fileOutputStream = new FileOutputStream(file);
                outputStream.writeTo(fileOutputStream);
            } catch (Exception e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }

        if (StringUtils.isNotBlank(documentDispatcherProperties.getOutput()) && !documentDispatcherProperties.getOutput().equalsIgnoreCase("file")) {
            Attachment attachment = MessageController.getInstance().createAttachment(new String(Base64Util.encodeBase64(outputStream.toByteArray(), false), "US-ASCII"), documentDispatcherProperties.getDocumentType().contains("pdf") ? "application/pdf" : "application/rtf");
            MessageController.getInstance().insertAttachment(attachment, connectorMessage.getChannelId(), connectorMessage.getMessageId());

            return attachment.getAttachmentId();
        }
        return null;
    }

    private void createPDF(Reader reader, OutputStream outputStream) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setEntityResolver(FSEntityResolver.instance());
        org.w3c.dom.Document doc = builder.parse(new InputSource(reader));

        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(doc, null);

            renderer.layout();
            renderer.createPDF(outputStream, true);
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    private void encryptPDF(InputStream inputStream, OutputStream outputStream, String password) throws Exception {
        PDDocument document = null;

        try {
            document = PDDocument.load(inputStream);

            AccessPermission accessPermission = new AccessPermission();
            accessPermission.setCanAssembleDocument(false);
            accessPermission.setCanExtractContent(true);
            accessPermission.setCanExtractForAccessibility(false);
            accessPermission.setCanFillInForm(false);
            accessPermission.setCanModify(false);
            accessPermission.setCanModifyAnnotations(false);
            accessPermission.setCanPrint(true);
            accessPermission.setCanPrintDegraded(true);

            String ownerPassword = System.currentTimeMillis() + "+" + Runtime.getRuntime().freeMemory() + "+" + (ownerPasswordSeq++);
            StandardProtectionPolicy policy = new StandardProtectionPolicy(ownerPassword, password, accessPermission);
            policy.setEncryptionKeyLength(128);
            document.protect(policy);

            document.save(outputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private void createRTF(InputStream inputStream, OutputStream outputStream) throws Exception {
        com.lowagie.text.Document document = null;

        try {
            document = new com.lowagie.text.Document();
            //TODO verify the character encoding

            RtfWriter2.getInstance(document, outputStream);

            document.open();
            HtmlParser parser = new HtmlParser();
            parser.go(document, inputStream);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private File createFile(String filename) throws IOException {
        File file = new File(filename);
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
        return file;
    }

}