/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;

import com.mirth.connect.model.Parameter;
import com.mirth.connect.model.Parameters;
import com.mirth.connect.model.codetemplates.CodeTemplateFunctionDefinition;

public class CodeTemplateUtil {

    private static Pattern COMMENT_PATTERN = Pattern.compile("^/\\*{2}([\\s\\S](?!\\*/))*[\\s\\S]?\\*/(\r\n|\r|\n)*");

    public static CodeTemplateDocumentation getDocumentation(String code) {
        String description = null;
        CodeTemplateFunctionDefinition functionDefinition = null;

        if (StringUtils.isNotBlank(code)) {
            try {
                FunctionVisitor visitor = new FunctionVisitor();
                CompilerEnvirons env = new CompilerEnvirons();
                env.setRecordingLocalJsDocComments(true);
                env.setAllowSharpComments(true);
                env.setRecordingComments(true);

                new Parser(env).parse(new StringReader(code), null, 1).visitAll(visitor);
                description = visitor.getDescription();
                functionDefinition = visitor.getFunctionDefinition();
            } catch (Exception e) {
                Matcher matcher = COMMENT_PATTERN.matcher(code);
                if (matcher.find()) {
                    description = matcher.group().replaceAll("^\\s*/\\*+\\s*|\\s*\\*+/\\s*$", "").trim();
                }
            }
        }

        return new CodeTemplateDocumentation(description, functionDefinition);
    }

    public static String updateCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            code = StringUtils.trim(code);
            int endIndex = 0;
            Matcher matcher = COMMENT_PATTERN.matcher(code);
            if (matcher.find()) {
                endIndex = matcher.end();
            }

            CodeTemplateDocumentation documentation = getDocumentation(code);
            String description = documentation.getDescription();
            CodeTemplateFunctionDefinition functionDefinition = documentation.getFunctionDefinition();

            if (StringUtils.isBlank(description)) {
                description = "Modify the description here. Modify the function name and parameters as needed. One function per template is recommended; create a new code template for each new function.";
            }

            StringBuilder builder = new StringBuilder("/**");

            for (String descriptionLine : description.split("\r\n|\r|\n")) {
                builder.append("\n\t").append(WordUtils.wrap(descriptionLine, 100, "\n\t", false));
            }

            if (functionDefinition != null) {
                builder.append('\n');

                if (CollectionUtils.isNotEmpty(functionDefinition.getParameters())) {
                    for (Parameter parameter : functionDefinition.getParameters()) {
                        StringBuilder parameterBuilder = new StringBuilder("\n\t@param {");
                        parameterBuilder.append(StringUtils.defaultString(parameter.getType(), "Any"));
                        parameterBuilder.append("} ").append(parameter.getName()).append(" - ").append(StringUtils.trimToEmpty(parameter.getDescription()));

                        builder.append(WordUtils.wrap(parameterBuilder.toString(), 100, "\n\t\t", false));
                    }
                }

                StringBuilder returnBuilder = new StringBuilder("\n\t@return {").append(StringUtils.defaultString(functionDefinition.getReturnType(), "Any")).append("} ").append(StringUtils.trimToEmpty(functionDefinition.getReturnDescription()));

                builder.append(WordUtils.wrap(returnBuilder.toString(), 100, "\n\t\t", false));
            }

            builder.append("\n*/\n");

            return builder.toString() + code.substring(endIndex);
        }

        return code;
    }

    public static String stripDocumentation(String code) {
        if (StringUtils.isNotBlank(code)) {
            code = StringUtils.trim(code);
            Matcher matcher = COMMENT_PATTERN.matcher(code);
            if (matcher.find()) {
                return StringUtils.trim(code.substring(matcher.end()));
            }
        }

        return code;
    }

    public static class CodeTemplateDocumentation {

        private String description;
        private CodeTemplateFunctionDefinition functionDefinition;

        public CodeTemplateDocumentation(String description, CodeTemplateFunctionDefinition functionDefinition) {
            this.description = description;
            this.functionDefinition = functionDefinition;
        }

        public String getDescription() {
            return description;
        }

        public CodeTemplateFunctionDefinition getFunctionDefinition() {
            return functionDefinition;
        }
    }

    private static class FunctionVisitor implements NodeVisitor {

        private static Pattern DOCUMENTATION_PATTERN = Pattern.compile("/\\*{2,}(?<desc>([^\r\n]|(\r\n|\r|\n)(?!\\*/|\\s*@))+)|(?<anno>(\r\n|\r|\n)\\s*@([^\r\n]|(\r\n|\r|\n)(?!\\*/|\\s*@))+)");
        private static Pattern ANNOTATION_PARAM_PATTERN = Pattern.compile("@param(\\s*\\{(?<type>[^\\}]*)\\})?\\s*(?<name>\\w+)\\s*(?:-\\s*)?(?<desc>[\\s\\S]*)");
        private static Pattern ANNOTATION_RETURN_PATTERN = Pattern.compile("@returns?(\\s*\\{(?<type>[^\\}]*)\\})?\\s*(?<desc>[\\s\\S]*)");

        private Comment commentNode;
        private boolean found;
        private String description;
        private CodeTemplateFunctionDefinition functionDefinition;

        public String getDescription() {
            return description;
        }

        public CodeTemplateFunctionDefinition getFunctionDefinition() {
            return functionDefinition;
        }

        @Override
        public boolean visit(AstNode node) {
            if (node instanceof AstRoot) {
                return true;
            }

            if (commentNode == null) {
                Comment comment = null;
                if (node instanceof Comment) {
                    comment = (Comment) node;
                } else {
                    comment = node.getJsDocNode();
                }

                if (comment != null) {
                    Matcher matcher = DOCUMENTATION_PATTERN.matcher(comment.getValue());
                    while (matcher.find()) {
                        String desc = matcher.group("desc");
                        if (StringUtils.isNotBlank(desc)) {
                            description = convertDesc(desc);
                        }
                    }

                    commentNode = comment;
                }
            }

            if (!found && node instanceof FunctionNode) {
                FunctionNode functionNode = (FunctionNode) node;
                functionDefinition = new CodeTemplateFunctionDefinition(functionNode.getFunctionName().getIdentifier());

                Map<String, Parameter> parameterMap = new LinkedHashMap<String, Parameter>();
                for (AstNode param : functionNode.getParams()) {
                    if (param instanceof Name) {
                        String paramName = ((Name) param).getIdentifier();
                        parameterMap.put(paramName, new Parameter(paramName));
                    }
                }

                Comment comment = null;
                if (commentNode != null) {
                    comment = commentNode;
                } else {
                    comment = functionNode.getJsDocNode();
                }

                if (comment != null) {
                    Matcher matcher = DOCUMENTATION_PATTERN.matcher(comment.getValue());

                    while (matcher.find()) {
                        String annotation = convertDesc(matcher.group("anno"));

                        if (StringUtils.startsWithIgnoreCase(annotation, "@param")) {
                            Matcher annotationMatcher = ANNOTATION_PARAM_PATTERN.matcher(annotation);

                            if (annotationMatcher.find()) {
                                Parameter parameter = parameterMap.get(annotationMatcher.group("name"));
                                if (parameter != null) {
                                    parameter.setType(StringUtils.trimToEmpty(annotationMatcher.group("type")));
                                    parameter.setDescription(convertDesc(annotationMatcher.group("desc")));
                                }
                            }
                        } else if (StringUtils.startsWithIgnoreCase(annotation, "@return")) {
                            Matcher annotationMatcher = ANNOTATION_RETURN_PATTERN.matcher(annotation);

                            if (annotationMatcher.find()) {
                                functionDefinition.setReturnType(StringUtils.trimToEmpty(annotationMatcher.group("type")));
                                functionDefinition.setReturnDescription(convertDesc(annotationMatcher.group("desc")));
                            }
                        }
                    }
                }

                functionDefinition.setParameters(new Parameters(parameterMap.values()));
                found = true;
            }

            return false;
        }

        private String convertDesc(String desc) {
            return StringUtils.trimToEmpty(desc).replaceAll("\\s*\\*+/\\s*$", "").replaceAll("[ \t\\x0B\f]*(\r\n|\r|\n)[ \t\\x0B\f]*", "\n").replaceAll("(?<=\\S)\n(?=\\S)", " ").replaceAll("\n{2,}", "\n\n");
        }
    }
}