package com.webreach.mirth.model.hl7v2.generators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

//Converts HL7 XML model to usable JAVA model based on template
public class HL7XMLtoJava {
	private static final String MESSAGE_TEMPLATE = "Message-template.txt";
	private static final String SEGMENT_TEMPLATE = "Segment-template.txt";
	private static final String COMPOSITE_TEMPLATE = "Composite-template.txt";
	public static String outputPath = "";
	public static String version = "";

	public static void main(String[] args) {
		String templatePath = args[2];
		try {
			String compositeTemplate = readFile(templatePath + "\\" + COMPOSITE_TEMPLATE);
			String segmentTemplate = readFile(templatePath + "\\" + SEGMENT_TEMPLATE);
			String messageTemplate = readFile(templatePath + "\\" + MESSAGE_TEMPLATE);

			String baseDirectory = args[0];
			String baseOutputPath = args[1];

			String[] versions = new String[] { "21", "22", "23", "231", "24", "25" };

			for (int x = 0; x < versions.length; x++) {
				version = versions[x];

				String directory = baseDirectory + "\\" + version;
				outputPath = baseOutputPath + "\\v" + version + "\\";
				new File(outputPath).mkdir();
				new File(outputPath + "composite").mkdir();
				new File(outputPath + "segment").mkdir();
				new File(outputPath + "message").mkdir();
				File[] files = listFiles(new File(directory));

				for (int i = 0; i < files.length; i++) {
					if (!files[i].isDirectory()) {
						String xmlString = readFile(files[i].getAbsolutePath());
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document document = builder.parse(new InputSource(new StringReader(xmlString)));

						if (files[i].getName().startsWith("message")) {
							ProcessMessage(document, messageTemplate);
						} else if (files[i].getName().startsWith("segment")) {
							ProcessSegment(document, segmentTemplate, false);
						} else if (files[i].getName().startsWith("composite")) {
							ProcessSegment(document, compositeTemplate, true);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void ProcessSegment(Document document, String template, boolean isComposite) throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		String name = document.getElementsByTagName("name").item(0).getTextContent();
		String description = document.getElementsByTagName("description").item(0).getTextContent();
		values.put("name", name);
		values.put("description", description);
		String fields = "";
		String fieldDescriptions = "";
		String repeats = "";
		String required = "";
		Element elementsRoot = (Element) document.getElementsByTagName("elements").item(0);

		NodeList fieldList = elementsRoot.getChildNodes();

		for (int i = 0; i < fieldList.getLength(); i++) {
			Node node = fieldList.item(i);

			if (node.getNodeName().equals("field")) {
				String fieldName = ((Element) node).getElementsByTagName("name").item(0).getTextContent();

				String fieldDescription = ((Element) node).getElementsByTagName("description").item(0).getTextContent();
				fieldDescription = fieldDescription.replaceAll("\\\"", "\\\\\"");
				String fieldDatatype = ((Element) node).getElementsByTagName("datatype").item(0).getTextContent();

				fields += "_" + fieldDatatype + ".class, ";
				fieldDescriptions += "\"" + fieldDescription + "\", ";
				NamedNodeMap nodeMap = node.getAttributes();
				if (nodeMap.getNamedItem("minOccurs") != null) {
					required += "false, ";
				} else {
					required += "true, ";
				}
				if (nodeMap.getNamedItem("maxOccurs") != null) {
					String repeatsVal = nodeMap.getNamedItem("maxOccurs").getNodeValue();
					if (repeatsVal.equals("unbounded")) {
						repeatsVal = "-1";
					}
					repeats += repeatsVal + ", ";
				} else {
					repeats += "0, ";
				}
			}
		}
		System.out.println(version + " " + name);
		try {
			values.put("fields", fields.substring(0, fields.length() - 2));
			values.put("fieldDescriptions", fieldDescriptions.substring(0, fieldDescriptions.length() - 2));
			values.put("repeats", repeats.substring(0, repeats.length() - 2));
			values.put("required", required.substring(0, required.length() - 2));
		} catch (Exception e) {
			if (name.equals("ED")) {
				// hack
				// Encapsulated Data doesn't have any fields...
				values.put("fields", "");
				values.put("fieldDescriptions", "");
				values.put("repeats", "");
				values.put("required", "");
			} else {
				e.printStackTrace();
				throw e;
			}
		}
		values.put("version", version);
		String path = "segment/";
		if (isComposite)
			path = "composite/";
		writeFile(new File(outputPath + path + "_" + name + ".java"), evaluate(template, values));
	}

	private static void ProcessMessage(Document document, String template) throws Exception {
		int knownSegments = 0;
		String name = document.getElementsByTagName("name").item(0).getTextContent();
		String description = document.getElementsByTagName("description").item(0).getTextContent();
		Map<String, String> values = new HashMap<String, String>();
		values.put("name", name);
		values.put("description", description);
		StringBuilder segments = new StringBuilder();
		StringBuilder repeats = new StringBuilder();
		StringBuilder required = new StringBuilder();
		StringBuilder groups = new StringBuilder();
		Element segmentsRoot = (Element) document.getElementsByTagName("segments").item(0);

		NodeList segmentList = segmentsRoot.getChildNodes();

		for (int i = 0; i < segmentList.getLength(); i++) {
			Node node = segmentList.item(i);

			if (node.getNodeName().equals("segment")) {
				String segmentName = node.getTextContent().trim();

				if (segmentName.equals("")) {
					continue;
				}

				knownSegments++;
				segments.append("_" + segmentName + ".class, ");
				NamedNodeMap nodeMap = node.getAttributes();

				if (nodeMap.getNamedItem("minOccurs") != null) {
					required.append("false, ");
				} else {
					required.append("true, ");
				}
				if (nodeMap.getNamedItem("maxOccurs") != null) {
					String repeatsVal = nodeMap.getNamedItem("maxOccurs").getNodeValue();
					if (repeatsVal.equals("unbounded")) {
						repeatsVal = "-1";
					}
					repeats.append(repeatsVal + ", ");
				} else {
					repeats.append("0, ");
				}

			} else if (node.getNodeName().equals("group")) {
				knownSegments = handleNestedGroups(repeats, required, groups, knownSegments, node, segments);
			}
		}

		if (groups.length() > 2) {
			String groupString = groups.toString().substring(0, groups.length() - 2);
			values.put("groups", groupString);
		} else {
			values.put("groups", "");
		}

		values.put("segments", segments.toString().substring(0, segments.length() - 2));
		values.put("repeats", repeats.toString().substring(0, repeats.length() - 2));
		values.put("required", required.toString().substring(0, required.length() - 2));
		values.put("version", version);
		writeFile(new File(outputPath + "message/" + "_" + name + ".java"), evaluate(template, values));
	}

	private static int handleNestedGroups(StringBuilder repeats, StringBuilder required, StringBuilder groups, int knownSegments, Node node, StringBuilder segments) {
		Element groupRoot = (Element) node;
		NodeList groupSegmentList = groupRoot.getChildNodes();
		int startSegment = knownSegments + 1;
		int j = 0;
		for (; j < groupSegmentList.getLength(); j++) {
			Node groupSegmentNode = groupSegmentList.item(j);
			if (groupSegmentNode.getNodeName().equals("group")) {
				knownSegments = handleNestedGroups(repeats, required, groups, knownSegments, groupSegmentNode, segments);
			} else if (groupSegmentNode.getNodeName().equals("segment")) {
				NamedNodeMap nodeMap = groupSegmentNode.getAttributes();
				String segmentName = groupSegmentNode.getTextContent().trim();
				if (segmentName.equals("")) {
					continue;
				}
				if (segmentName.equals("Hxx")) {
					segmentName = "ANY";
				}
				knownSegments++;
				segments.append("_" + segmentName + ".class, ");
				if (nodeMap.getNamedItem("minOccurs") != null) {
					required.append("false, ");
				} else {
					required.append("true, ");
				}
				if (nodeMap.getNamedItem("maxOccurs") != null) {
					String repeatsVal = nodeMap.getNamedItem("maxOccurs").getNodeValue();
					if (repeatsVal.equals("unbounded")) {
						repeatsVal = "-1";
					}
					repeats.append(repeatsVal + ", ");
				} else {
					repeats.append("0, ");
				}
			}
		}

		NamedNodeMap nodeMap = node.getAttributes();
		String gRequired = "1";
		String gRepeats = "0";
		if (nodeMap.getNamedItem("minOccurs") != null) {
			gRequired = "0";
		}
		if (nodeMap.getNamedItem("maxOccurs") != null) {
			gRepeats = "1";
		}

		groups.append("{" + startSegment + ", " + knownSegments + ", " + gRequired + ", " + gRepeats + "}, ");
		return knownSegments;
	}

	public static String readFile(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder contents = new StringBuilder();
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				contents.append(line + "\n");
			}
		} finally {
			reader.close();
		}

		return contents.toString();
	}

	public static File[] listFiles(File readDirectory) {
		File[] todoFiles = new File[0];
		try {
			todoFiles = readDirectory.listFiles();
		} catch (Exception e) {

		}
		return todoFiles;
	}

	public static void writeXmlFile(Document doc, String filename) {
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			File file = new File(filename);
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(File file, String data) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		try {
			writer.write(data);
			writer.flush();
		} finally {
			writer.close();
		}
	}

	public static String evaluate(String template, Map<String, String> values) {
		VelocityContext context = new VelocityContext();

		// load the context
		for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			context.put(entry.getKey().toString(), entry.getValue());
		}

		StringWriter writer = new StringWriter();

		try {
			Velocity.init();
			Velocity.evaluate(context, writer, "LOG", template);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

}
