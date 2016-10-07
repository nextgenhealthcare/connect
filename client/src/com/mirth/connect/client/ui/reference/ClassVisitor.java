/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.fife.rsta.ac.js.IconFactory;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.model.Parameters;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateFunctionDefinition;

public class ClassVisitor extends VoidVisitorAdapter<Object> {

    private static final Pattern JAVADOC_ANNOTATION_PATTERN = Pattern.compile("(?<!\\{)@(?<key>\\w+)\\s+(?<value>([^@]|(?<=\\{)@)*)");

    private List<String> inputTextList;
    private List<Reference> references = new ArrayList<Reference>();

    public ClassVisitor(List<String> inputTextList) {
        this.inputTextList = inputTextList;
    }

    public static List<Reference> getReferencesByCompilationUnit(CompilationUnit compilationUnit, List<String> inputTextList) {
        ClassVisitor visitor = new ClassVisitor(inputTextList);
        visitor.visit(compilationUnit, null);
        return visitor.getReferences();
    }

    @Override
    public void visit(PackageDeclaration n, Object arg) {
        n.getName().accept(this, arg);
    }

    public void visit(QualifiedNameExpr n, Object arg) {
        n.getQualifier().accept(this, arg);
        if (arg instanceof StringBuilder) {
            ((StringBuilder) arg).append('.');
        }
        visit((NameExpr) n, arg);
    }

    @Override
    public void visit(NameExpr n, Object arg) {
        if (arg instanceof StringBuilder) {
            ((StringBuilder) arg).append(n.getName());
        }
    }

    @Override
    public void visit(CompilationUnit n, Object arg) {
        String packageName = null;
        if (n.getPackage() != null) {
            StringBuilder builder = new StringBuilder();
            visit(n.getPackage(), builder);
            packageName = builder.toString();
        }

        super.visit(n, packageName);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        String className = n.getName();
        String packageName = (String) arg;

        StringBuilder builder = new StringBuilder();
        builder.append("<html><body><b>");

        if (StringUtils.isNotBlank(packageName)) {
            builder.append("package ");
            builder.append(packageName);
        }

        builder.append("<br/><h3><a href=\"");
        builder.append(PlatformUI.SERVER_URL);
        builder.append(UIConstants.USER_API_LOCATION);

        if (StringUtils.isNotBlank(packageName)) {
            builder.append(packageName.replace(".", "/"));
            builder.append('/');
            builder.append(className);
            builder.append(".html");
        }

        builder.append("\">");
        builder.append(n.isInterface() ? "Interface " : "Class ");
        builder.append(className);
        builder.append("</a></h3></b><hr/><code>");

        if (ModifierSet.isPublic(n.getModifiers())) {
            builder.append("public ");
        } else {
            // Don't add references for non-public classes
            return;
        }

        if (ModifierSet.isStatic(n.getModifiers())) {
            builder.append("static ");
        }
        if (ModifierSet.isAbstract(n.getModifiers())) {
            builder.append("abstract ");
        }
        if (ModifierSet.isFinal(n.getModifiers())) {
            builder.append("final ");
        }
        if (ModifierSet.isNative(n.getModifiers())) {
            builder.append("native ");
        }
        if (ModifierSet.isSynchronized(n.getModifiers())) {
            builder.append("synchronized ");
        }
        if (ModifierSet.isTransient(n.getModifiers())) {
            builder.append("transient ");
        }
        if (ModifierSet.isVolatile(n.getModifiers())) {
            builder.append("volatile ");
        }

        if (n.isInterface()) {
            builder.append("interface ");
        } else {
            builder.append("class ");
        }

        builder.append(className);

        List<ClassOrInterfaceType> extendsList = n.getExtends();
        if (CollectionUtils.isNotEmpty(extendsList)) {
            ClassOrInterfaceType type = extendsList.get(0);
            builder.append("<br/>extends ");
            builder.append(type.getName());
        }

        List<ClassOrInterfaceType> implementsList = n.getImplements();
        if (CollectionUtils.isNotEmpty(implementsList)) {
            builder.append("<br/>implements ");
            for (Iterator<ClassOrInterfaceType> it = implementsList.iterator(); it.hasNext();) {
                ClassOrInterfaceType type = it.next();
                builder.append(type.getName());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
        }

        builder.append("</code><br/><br/>");

        if (n.getJavaDoc() != null) {
            String comment = StringUtils.trim(n.getJavaDoc().getContent());
            if (StringUtils.isNotBlank(comment)) {
                builder.append(encode(convertComment(comment)));
            }
        }

        String summary = builder.toString();

        references.add(new ClassReference(CodeTemplateContextSet.getGlobalContextSet(), null, className, inputTextList, summary));

        if (n.getMembers() != null) {
            for (BodyDeclaration member : n.getMembers()) {
                member.accept(this, className);
            }
        }
    }

    @Override
    public void visit(ConstructorDeclaration n, Object arg) {
        addMethod(true, (String) arg, n.getName(), n.getName(), n.getModifiers(), n.getJavaDoc(), n.getParameters());
    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        addMethod(false, (String) arg, n.getName(), n.getType().toString(), n.getModifiers(), n.getJavaDoc(), n.getParameters());
    }

    private void addMethod(boolean constructor, String className, String name, String type, int modifiers, JavadocComment javadoc, List<Parameter> parameters) {
        String iconName = null;
        if (ModifierSet.isPublic(modifiers)) {
            if (ModifierSet.isStatic(modifiers)) {
                iconName = IconFactory.PUBLIC_STATIC_FUNCTION_ICON;
            } else {
                iconName = IconFactory.PUBLIC_METHOD_ICON;
            }
        } else {
            // Don't add references for non-public methods
            return;
        }

        String comment = null;
        Map<String, String> parameterComments = new CaseInsensitiveMap();
        String returnComment = constructor ? "A new " + name + " object." : null;
        String deprecatedComment = null;

        if (javadoc != null) {
            comment = StringUtils.trim(javadoc.getContent());
            if (StringUtils.isNotBlank(comment)) {
                Matcher matcher = JAVADOC_ANNOTATION_PATTERN.matcher(comment);
                while (matcher.find() && matcher.groupCount() >= 2) {
                    String key = matcher.group("key");
                    String value = convertComment(matcher.group("value"));

                    if (key.equalsIgnoreCase("param")) {
                        int index = value.indexOf(' ');
                        if (index >= 0) {
                            key = value.substring(0, index).trim();
                            value = value.substring(index).trim();
                        } else {
                            key = value;
                            value = "";
                        }

                        parameterComments.put(key, value);
                    } else if (key.equalsIgnoreCase("return")) {
                        returnComment = value;
                    } else if (key.equalsIgnoreCase("deprecated")) {
                        deprecatedComment = value;
                    }
                }

                comment = convertComment(comment);

                if (StringUtils.isNotBlank(deprecatedComment)) {
                    comment = "<b>Deprecated.</b> <em>" + deprecatedComment + "</em><br/><br/>" + comment;
                }
            }
        }

        Parameters params = new Parameters();
        if (CollectionUtils.isNotEmpty(parameters)) {
            for (Parameter parameter : parameters) {
                String parameterName = parameter.getId().getName();
                params.add(parameterName, parameter.getType().toString(), parameterComments.get(parameterName));
            }
        }

        Reference reference;
        if (constructor) {
            reference = new ConstructorReference(CodeTemplateContextSet.getGlobalContextSet(), null, name, name, comment, null, new CodeTemplateFunctionDefinition(name, params, type, returnComment));
        } else {
            reference = new FunctionReference(CodeTemplateContextSet.getGlobalContextSet(), null, className, name, comment, null, new CodeTemplateFunctionDefinition(name, params, type, returnComment), inputTextList);
        }

        if (StringUtils.isNotBlank(deprecatedComment)) {
            reference.setDeprecated(true);
        }

        reference.setIconName(iconName);
        references.add(reference);
    }

    public List<Reference> getReferences() {
        return references;
    }

    private String convertComment(String comment) {
        return comment.replaceAll("(?m)^[\\s*]*", "").replaceAll("\\{@\\w*\\s*(#[^\\}\\s]*)?\\s*(?<value>[^\\}]*)\\s*\\}", "${value}").replaceAll("(\r\n|\r|\n)@[\\S\\s]*", "").replaceAll("\r\n|\r|\n", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private String encode(String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;");
    }
}