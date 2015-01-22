/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.token;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.mirth.connect.client.ui.components.rsta.token.js.MirthJavaScriptTokenMaker;

/**
 * We need to extend the abstract factory class here rather than extending the default factory,
 * because the default factory class is not public.
 */
public class MirthTokenMakerFactory extends AbstractTokenMakerFactory implements SyntaxConstants {

    @Override
    protected void initTokenMakerMap() {

        String pkg = "org.fife.ui.rsyntaxtextarea.modes.";

        // @formatter:off
        putMapping(SYNTAX_STYLE_NONE,           pkg + "PlainTextTokenMaker");
        putMapping(SYNTAX_STYLE_ACTIONSCRIPT,   pkg + "ActionScriptTokenMaker");
        putMapping(SYNTAX_STYLE_ASSEMBLER_X86,  pkg + "AssemblerX86TokenMaker");
        putMapping(SYNTAX_STYLE_BBCODE,         pkg + "BBCodeTokenMaker");
        putMapping(SYNTAX_STYLE_C,              pkg + "CTokenMaker");
        putMapping(SYNTAX_STYLE_CLOJURE,        pkg + "ClojureTokenMaker");
        putMapping(SYNTAX_STYLE_CPLUSPLUS,      pkg + "CPlusPlusTokenMaker");
        putMapping(SYNTAX_STYLE_CSHARP,         pkg + "CSharpTokenMaker");
        putMapping(SYNTAX_STYLE_CSS,            pkg + "CSSTokenMaker");
        putMapping(SYNTAX_STYLE_D,              pkg + "DTokenMaker");
        putMapping(SYNTAX_STYLE_DART,           pkg + "DartTokenMaker");
        putMapping(SYNTAX_STYLE_DELPHI,         pkg + "DelphiTokenMaker");
        putMapping(SYNTAX_STYLE_DTD,            pkg + "DtdTokenMaker");
        putMapping(SYNTAX_STYLE_FORTRAN,        pkg + "FortranTokenMaker");
        putMapping(SYNTAX_STYLE_GROOVY,         pkg + "GroovyTokenMaker");
        putMapping(SYNTAX_STYLE_HTACCESS,       pkg + "HtaccessTokenMaker");
        putMapping(SYNTAX_STYLE_HTML,           pkg + "HTMLTokenMaker");
        putMapping(SYNTAX_STYLE_JAVA,           pkg + "JavaTokenMaker");
        putMapping(SYNTAX_STYLE_JAVASCRIPT,     MirthJavaScriptTokenMaker.class.getName());
        putMapping(SYNTAX_STYLE_JSON,           pkg + "JsonTokenMaker");
        putMapping(SYNTAX_STYLE_JSP,            pkg + "JSPTokenMaker");
        putMapping(SYNTAX_STYLE_LATEX,          pkg + "LatexTokenMaker");
        putMapping(SYNTAX_STYLE_LISP,           pkg + "LispTokenMaker");
        putMapping(SYNTAX_STYLE_LUA,            pkg + "LuaTokenMaker");
        putMapping(SYNTAX_STYLE_MAKEFILE,       pkg + "MakefileTokenMaker");
        putMapping(SYNTAX_STYLE_MXML,           pkg + "MxmlTokenMaker");
        putMapping(SYNTAX_STYLE_NSIS,           pkg + "NSISTokenMaker");
        putMapping(SYNTAX_STYLE_PERL,           pkg + "PerlTokenMaker");
        putMapping(SYNTAX_STYLE_PHP,            pkg + "PHPTokenMaker");
        putMapping(SYNTAX_STYLE_PROPERTIES_FILE,pkg + "PropertiesFileTokenMaker");
        putMapping(SYNTAX_STYLE_PYTHON,         pkg + "PythonTokenMaker");
        putMapping(SYNTAX_STYLE_RUBY,           pkg + "RubyTokenMaker");
        putMapping(SYNTAX_STYLE_SAS,            pkg + "SASTokenMaker");
        putMapping(SYNTAX_STYLE_SCALA,          pkg + "ScalaTokenMaker");
        putMapping(SYNTAX_STYLE_SQL,            pkg + "SQLTokenMaker");
        putMapping(SYNTAX_STYLE_TCL,            pkg + "TclTokenMaker");
        putMapping(SYNTAX_STYLE_UNIX_SHELL,     pkg + "UnixShellTokenMaker");
        putMapping(SYNTAX_STYLE_VISUAL_BASIC,   pkg + "VisualBasicTokenMaker");
        putMapping(SYNTAX_STYLE_WINDOWS_BATCH,  pkg + "WindowsBatchTokenMaker");
        putMapping(SYNTAX_STYLE_XML,            pkg + "XMLTokenMaker");
        // @formatter:on
    }
}
