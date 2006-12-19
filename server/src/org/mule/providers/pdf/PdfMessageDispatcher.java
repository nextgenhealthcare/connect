package org.mule.providers.pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

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
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.server.util.UUIDGenerator;

public class PdfMessageDispatcher extends AbstractMessageDispatcher {
	private PdfConnector connector;

	private MessageObjectController messageObjectController = new MessageObjectController();

	public PdfMessageDispatcher(PdfConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		String endpoint = event.getEndpoint().getEndpointURI().getAddress();
		MessageObject messageObject = null;
		try {
			Object data = event.getTransformedMessage();
			if (data == null) {
				return;
			} else if (data instanceof MessageObject) {
				messageObject = (MessageObject) data;
				
				if (messageObject.getStatus().equals(MessageObject.Status.REJECTED)){
					return;
				}
				if (messageObject.getCorrelationId() == null){
					//If we have no correlation id, this means this is the original message
					//so let's copy it and assign a new id and set the proper correlationid
					MessageObject clone = (MessageObject) messageObject.clone();
					clone.setId(UUIDGenerator.getUUID());
					clone.setDateCreated(Calendar.getInstance());
					clone.setCorrelationId(messageObject.getId());
					clone.setConnectorName(new ChannelController().getDestinationName(this.getConnector().getName()));
					messageObject = clone;
				}
				String filename = (String) event.getProperty(PdfConnector.PROPERTY_FILENAME);

				if (filename == null) {
					String pattern = (String) event.getProperty(PdfConnector.PROPERTY_OUTPUT_PATTERN);

					if (pattern == null) {
						pattern = connector.getOutputPattern();
					}

					filename = generateFilename(event, pattern, messageObject);
				}

				if (filename == null) {
					throw new IOException("Filename is null");
				}

				String template = replacer.replaceValues(connector.getTemplate(), messageObject, filename);
				File file = Utility.createFile(endpoint + "/" + filename);
				logger.info("Writing PDF to: " + file.getAbsolutePath());
				writeDocument(template, file, messageObject);

				// update the message status to sent
				messageObject.setStatus(MessageObject.Status.SENT);
				messageObjectController.updateMessage(messageObject);
			} else {
				logger.warn("received data is not of expected type");
			}
		} catch (Exception e) {
			if (messageObject != null){
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + "Error writing PDF\n" +  StackTracePrinter.stackTraceToString(e));
				messageObjectController.updateMessage(messageObject);
			}
			connector.handleException(e);
		}
	}

	private void writeDocument(String template, File file, MessageObject messageObject) throws Exception {
		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

			if (connector.isEncrypted() && (connector.getPassword() != null)) {
				writer.setEncryption(PdfWriter.STRENGTH128BITS, connector.getPassword(), null, PdfWriter.AllowCopy | PdfWriter.AllowPrinting | PdfWriter.AllowFillIn);
			}

			// add tags to the template to create a valid HTML document
			StringBuilder contents = new StringBuilder();
			contents.append("<html>");
			contents.append("<body>");
			contents.append(template);
			contents.append("</body>");
			contents.append("</html>");

			ByteArrayInputStream bais = new ByteArrayInputStream(contents.toString().getBytes());
			document.open();
			HtmlParser parser = new HtmlParser();
			
			parser.go(document, bais);
		} catch (Exception e) {
			if (messageObject != null){
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + "Error writing PDF\n" +  StackTracePrinter.stackTraceToString(e));
				messageObjectController.updateMessage(messageObject);
			}
			connector.handleException(e);
		} finally {
			document.close();
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
