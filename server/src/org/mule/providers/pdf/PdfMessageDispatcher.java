package org.mule.providers.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.Utility;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import com.webreach.mirth.model.MessageObject;

public class PdfMessageDispatcher extends AbstractMessageDispatcher {
	private PdfConnector connector;

	public PdfMessageDispatcher(PdfConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		try {
			TemplateValueReplacer replacer = new TemplateValueReplacer();
			String endpoint = event.getEndpoint().getEndpointURI().getAddress();
			Object data = event.getTransformedMessage();
			String filename = (String) event.getProperty(PdfConnector.PROPERTY_FILENAME);

			if (filename == null) {
				String outPattern = (String) event.getProperty(PdfConnector.PROPERTY_OUTPUT_PATTERN);

				if (outPattern == null) {
					outPattern = connector.getOutputPattern();
				}

				filename = generateFilename(event, outPattern);
			}

			if (filename == null) {
				throw new IOException("Filename is null");
			}

			File file = Utility.createFile(endpoint + "/" + filename);
			String template = connector.getTemplate();

			if (data instanceof MessageObject) {
				MessageObject messageObject = (MessageObject) data;
				template = replacer.replaceValues(template, messageObject, filename);
			}

			logger.info("Writing PDF to: " + file.getAbsolutePath());
			writeDocument(template, file);
		} catch (Exception e) {
			getConnector().handleException(e);
		}
	}

	private String generateFilename(UMOEvent event, String pattern) {
		if (pattern == null) {
			pattern = connector.getOutputPattern();
		}

		return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
	}

	private void writeDocument(String template, File file) throws Exception {
		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			
			if (connector.isEncrypted()) {
				writer.setEncryption(PdfWriter.STRENGTH128BITS, connector.getPassword(), null, PdfWriter.AllowCopy | PdfWriter.AllowPrinting | PdfWriter.AllowFillIn);	
			}
			
			document.open();
			BBCodeParser parser = new BBCodeParser(template);
			
			while (parser.hasNext()) {
				BBCodeToken token = parser.getNext();
				Phrase phrase;
				
				if (token.getType().equals("BOLD")) {
					phrase = new Phrase(token.getValue(), new Font(Font.TIMES_ROMAN, Font.DEFAULTSIZE, Font.BOLD));
				} else if (token.getType().equals("ITALIC")) {
					phrase = new Phrase(token.getValue(), new Font(Font.TIMES_ROMAN, Font.DEFAULTSIZE, Font.ITALIC));
				} else if (token.getType().equals("UNDERLINE")) {
					phrase = new Phrase(token.getValue(), new Font(Font.TIMES_ROMAN, Font.DEFAULTSIZE, Font.UNDERLINE));
				} else {
					phrase = new Phrase(token.getValue(), new Font(Font.TIMES_ROMAN, Font.DEFAULTSIZE));
				}
				
				document.add(phrase);
			}
		} catch (Exception e) {
			throw e;
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

	public void doDispose() {}
}
