package org.mule.providers.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.Utility;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
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
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		String endpoint = event.getEndpoint().getEndpointURI().getAddress();

		try {
			Object data = event.getTransformedMessage();

			if (data instanceof MessageObject) {
				MessageObject messageObject = (MessageObject) data;
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
				writeDocument(template, file);
			} else {
				logger.warn("received data is not of expected type");
			}
		} catch (Exception e) {
			connector.handleException(e);
		}
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
				List<BBCodeParser.KeywordType> tags = token.getApplicableTags();

				if (tags.contains(BBCodeParser.KeywordType.NEWPAGE)) {
					document.newPage();
				} else if (tags.contains(BBCodeParser.KeywordType.IMAGE)) {
					Image image = Image.getInstance(token.getValue());
					document.add(image);
				} else if (tags.contains(BBCodeParser.KeywordType.PRE)) {
					document.add(new Phrase(token.getValue(), new Font(Font.TIMES_ROMAN, Font.DEFAULTSIZE, Font.NORMAL)));
				} else {
					// set the style
					int fontStyle = Font.NORMAL;

					for (Iterator iter = tags.iterator(); iter.hasNext();) {
						BBCodeParser.KeywordType keyword = (BBCodeParser.KeywordType) iter.next();

						if (keyword.equals(BBCodeParser.KeywordType.BOLD)) {
							fontStyle |= Font.BOLD;
						} else if (keyword.equals(BBCodeParser.KeywordType.ITALIC)) {
							fontStyle |= Font.ITALIC;
						} else if (keyword.equals(BBCodeParser.KeywordType.UNDERLINE)) {
							fontStyle |= Font.UNDERLINE;
						} else if (keyword.equals(BBCodeParser.KeywordType.STRIKETHRU)) {
							fontStyle |= Font.STRIKETHRU;
						}
					}

					// set the size
					int fontSize = Font.DEFAULTSIZE;

					if (token.getSize() > 0) {
						fontSize = token.getSize();
					}

					// set the color
					Color fontColor = Color.BLACK;
					
					if (token.getColor() != null) {
						fontColor = Color.getColor(token.getColor());
					}
					
					document.add(new Phrase(token.getValue(), new Font(Font.TIMES_ROMAN, fontSize, fontStyle, fontColor)));
				}
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

	private String generateFilename(UMOEvent event, String pattern, MessageObject messageObject) {
		if (connector.getFilenameParser() instanceof VariableFilenameParser) {
			VariableFilenameParser filenameParser = (VariableFilenameParser) connector.getFilenameParser();
			filenameParser.setMessageObject(messageObject);
			return filenameParser.getFilename(event.getMessage(), pattern);
		} else {
			return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
		}
	}
	
	public void doDispose() {}
}
