/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;

import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class DocumentDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private DocumentDispatcherProperties connectorProperties;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.WRITER;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (DocumentDispatcherProperties) getConnectorProperties();

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() throws UndeployException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStop() throws StopException {
        // TODO Auto-generated method stub
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        DocumentDispatcherProperties documentDispatcherProperties = (DocumentDispatcherProperties) SerializationUtils.clone(connectorProperties);

        documentDispatcherProperties.setHost(replacer.replaceValues(documentDispatcherProperties.getHost(), connectorMessage));
        documentDispatcherProperties.setOutputPattern(replacer.replaceValues(documentDispatcherProperties.getOutputPattern(), connectorMessage));
        documentDispatcherProperties.setPassword(replacer.replaceValues(documentDispatcherProperties.getPassword(), connectorMessage));
        documentDispatcherProperties.setTemplate(replacer.replaceValues(documentDispatcherProperties.getTemplate(), connectorMessage));

        return documentDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        DocumentDispatcherProperties documentDispatcherProperties = (DocumentDispatcherProperties) connectorProperties;
        String responseData = null;
        String responseError = null;
        Status responseStatus = Status.QUEUED;

        String info = "";
        if (documentDispatcherProperties.isEncrypt()) {
            info = "Encrypted ";
        }
        info += documentDispatcherProperties.getDocumentType() + " Document Type Result Written To: " + documentDispatcherProperties.getHost() + "/" + documentDispatcherProperties.getOutputPattern();

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, info);

        try {
            File file = createFile(documentDispatcherProperties.getHost() + "/" + documentDispatcherProperties.getOutputPattern());
            logger.info("Writing document to: " + file.getAbsolutePath());
            writeDocument(documentDispatcherProperties.getTemplate(), file, documentDispatcherProperties);

            responseData = "Document successfully written: " + documentDispatcherProperties.getOutputPattern();
            responseStatus = Status.SENT;
        } catch (Exception e) {
            alertController.sendAlerts(getChannelId(), Constants.ERROR_401, "Error writing document", e);
            responseData = ErrorMessageBuilder.buildErrorResponse("Error writing document", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_401, "Error writing document", e);

            // TODO: Handle exception
//            connector.handleException(e);
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }

        return new Response(responseStatus, responseData, responseError);
    }

    private void writeDocument(String template, File file, DocumentDispatcherProperties documentDispatcherProperties) throws Exception {
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

        if (documentDispatcherProperties.getDocumentType().toLowerCase().equals("pdf")) {
            FileOutputStream renderFos = null;

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader(contents.toString())));

                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocument(document, null);
                renderFos = new FileOutputStream(file);
                renderer.layout();
                renderer.createPDF(renderFos, true);
            } catch (Throwable e) {
                throw new Exception(e);
            } finally {
                if (renderFos != null) {
                    renderFos.close();
                }
            }

            if (documentDispatcherProperties.isEncrypt() && (documentDispatcherProperties.getPassword() != null)) {
                FileInputStream encryptFis = null;
                FileOutputStream encryptFos = null;

                try {
                    encryptFis = new FileInputStream(file);
                    PdfReader reader = new PdfReader(encryptFis);
                    encryptFos = new FileOutputStream(file);
                    PdfEncryptor.encrypt(reader, encryptFos, true, documentDispatcherProperties.getPassword(), null, PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY);
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (encryptFis != null) {
                        encryptFis.close();
                    }

                    if (encryptFos != null) {
                        encryptFos.close();
                    }
                }
            }
        } else if (documentDispatcherProperties.getDocumentType().toLowerCase().equals("rtf")) {
            com.lowagie.text.Document document = null;

            try {
                document = new com.lowagie.text.Document();
                ByteArrayInputStream bais = new ByteArrayInputStream(contents.toString().getBytes());
                RtfWriter2.getInstance(document, new FileOutputStream(file));
                document.open();
                HtmlParser parser = new HtmlParser();
                parser.go(document, bais);
            } finally {
                if (document != null) {
                    document.close();
                }
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
