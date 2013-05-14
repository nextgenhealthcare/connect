package com.mirth.connect.model.generator;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class HL7ModelGenerator {
    private static final String MESSAGE_TEMPLATE = "Message.txt";
    private static final String SEGMENT_TEMPLATE = "Segment.txt";
    private static final String COMPOSITE_TEMPLATE = "Composite.txt";
    private static final String[] VERSIONS = new String[] { "21", "22", "23", "231", "24", "25" };

    public String outputPath = "";
    public String version = "";

    public static void main(String[] args) {
        String baseDirectory = args[0];
        String baseOutputPath = args[1];
        String templatePath = args[2];

        HL7ModelGenerator generator = new HL7ModelGenerator();
        generator.generateModel(baseDirectory, baseOutputPath, templatePath);
    }

    public void generateModel(String baseDirectory, String baseOutputPath, String templatePath) {
        try {
            String compositeTemplate = FileUtils.readFileToString(new File(templatePath + File.separator + COMPOSITE_TEMPLATE));
            String segmentTemplate = FileUtils.readFileToString(new File(templatePath + File.separator + SEGMENT_TEMPLATE));
            String messageTemplate = FileUtils.readFileToString(new File(templatePath + File.separator + MESSAGE_TEMPLATE));

            for (int x = 0; x < VERSIONS.length; x++) {
                version = VERSIONS[x];
                outputPath = baseOutputPath + File.separator + "v" + version + File.separator;

                new File(outputPath).mkdir();
                new File(outputPath + "composite").mkdir();
                new File(outputPath + "segment").mkdir();
                new File(outputPath + "message").mkdir();

                File[] files = new File(baseDirectory + File.separator + version).listFiles();

                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory()) {
                        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document document = builder.parse(new InputSource(new StringReader(FileUtils.readFileToString(new File(files[i].getAbsolutePath())))));

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

    private void ProcessSegment(Document document, String template, boolean isComposite) throws Exception {
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
                String fieldDescription = ((Element) node).getElementsByTagName("description").item(0).getTextContent().replaceAll("\\\"", "\\\\\"");
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
                throw e;
            }
        }

        values.put("version", version);
        String path = "segment/";

        if (isComposite) {
            path = "composite/";
        }

        FileUtils.writeStringToFile(new File(outputPath + path + "_" + name + ".java"), evaluate(template, values));
    }

    private void ProcessMessage(Document document, String template) throws Exception {
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

        FileUtils.writeStringToFile(new File(outputPath + "message/" + "_" + name + ".java"), evaluate(template, values));
    }

    private int handleNestedGroups(StringBuilder repeats, StringBuilder required, StringBuilder groups, int knownSegments, Node node, StringBuilder segments) {
        Element groupRoot = (Element) node;
        NodeList groupSegmentList = groupRoot.getChildNodes();
        int startSegment = knownSegments + 1;

        for (int i = 0; i < groupSegmentList.getLength(); i++) {
            Node groupSegmentNode = groupSegmentList.item(i);
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

    private String evaluate(String template, Map<String, String> values) {
        VelocityContext context = new VelocityContext();

        for (Entry<String, String> entry : values.entrySet()) {
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
