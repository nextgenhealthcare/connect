package com.webreach.mirth.connectors.doc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.Utility;

import com.lowagie.text.Document;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class DocumentMessageDispatcher extends AbstractMessageDispatcher {
	private DocumentConnector connector;

	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = new AlertController();
	
	public DocumentMessageDispatcher(DocumentConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		String endpoint = event.getEndpoint().getEndpointURI().getAddress();
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);

		if (messageObject == null) {
			return;
		}

		try {
			String filename = (String) event.getProperty(DocumentConnector.PROPERTY_FILENAME);

			if (filename == null) {
				String pattern = (String) event.getProperty(DocumentConnector.PROPERTY_OUTPUT_PATTERN);

				if (pattern == null) {
					pattern = connector.getOutputPattern();
				}

				filename = generateFilename(event, pattern, messageObject);
			}

			if (filename == null) {
				throw new IOException("Filename is null");
			}

			String template = replacer.replaceValues(connector.getTemplate(), messageObject);
			File file = Utility.createFile(endpoint + "/" + filename);
			logger.info("Writing document to: " + file.getAbsolutePath());
			writeDocument(template, file, messageObject);

			// update the message status to sent
			messageObjectController.setSuccess(messageObject, "Document successfully written: " + filename);
		} catch (Exception e) {
			alertController.sendAlerts(((DocumentConnector) connector).getChannelId(), Constants.ERROR_401, "Error writing document", e);
			messageObjectController.setError(messageObject, Constants.ERROR_401, "Error writing document", e);
			connector.handleException(e);
		}
	}

	private void writeDocument(String template, File file, MessageObject messageObject) throws Exception {
		Document document = new Document();
		ByteArrayInputStream bais = null;
		
		try {
			if (connector.getDocumentType().equals("pdf")) {
				PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
				
				if (connector.isEncrypted() && (connector.getPassword() != null)) {
					writer.setEncryption(PdfWriter.STRENGTH128BITS, connector.getPassword(), null, PdfWriter.AllowCopy | PdfWriter.AllowPrinting | PdfWriter.AllowFillIn);
				}
			} else if (connector.getDocumentType().equals("rtf")) {
				RtfWriter2.getInstance(document, new FileOutputStream(file));
			}

			// add tags to the template to create a valid HTML document
			StringBuilder contents = new StringBuilder();
			contents.append("<html>");
			contents.append("<body>");
			contents.append(template);
			contents.append("</body>");
			contents.append("</html>");

			bais = new ByteArrayInputStream(contents.toString().getBytes());
			document.open();
			HtmlParser parser = new HtmlParser();
			parser.go(document, bais);
		} finally {
			try {
				document.close();
			} catch (Exception e) {
				logger.warn("could not close document", e);
			}
		}
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		return null;
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	private String generateFilename(UMOEvent event, String pattern, MessageObject messageObject) {
		if (connector.getFilenameParser() instanceof VariableFilenameParser) {
			VariableFilenameParser filenameParser = (VariableFilenameParser) connector.getFilenameParser();
			filenameParser.setMessageObject(messageObject);
			return filenameParser.getFilename(event.getMessage(), pattern);
		} else {
			return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
		}
	}

	public void doDispose() {

	}
}
