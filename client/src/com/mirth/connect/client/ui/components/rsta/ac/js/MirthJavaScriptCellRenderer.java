/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.fife.rsta.ac.js.JavaScriptCellRenderer;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.autocomplete.Util;

import com.mirth.connect.client.ui.components.rsta.ac.MirthFunctionCompletion;

public class MirthJavaScriptCellRenderer extends JavaScriptCellRenderer {

    private static final String PREFIX = "<html><nobr>";

    private String typeColor;
    private String paramColor;

    public MirthJavaScriptCellRenderer() {
        typeColor = "#808080";
        paramColor = createParamColor();
    }

    @Override
    public void setTypeColor(Color color) {
        super.setTypeColor(color);
        if (color != null) {
            typeColor = Util.getHexString(color);
        }
    }

    @Override
    public void setParamColor(Color color) {
        super.setParamColor(color);
        if (color != null) {
            paramColor = Util.getHexString(color);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        paramColor = createParamColor();
    }

    @Override
    protected void prepareForFunctionCompletion(JList list, FunctionCompletion functionCompletion, int index, boolean selected, boolean hasFocus) {
        StringBuilder builder = new StringBuilder(PREFIX);

        if (functionCompletion instanceof MirthFunctionCompletion && ((MirthFunctionCompletion) functionCompletion).isDeprecated()) {
            builder.append("<strike>");
            builder.append(functionCompletion.getName());
            builder.append("</strike>");
        } else {
            builder.append(functionCompletion.getName());
        }

        char paramListStart = functionCompletion.getProvider().getParameterListStart();
        if (paramListStart != 0) {
            builder.append(paramListStart);
        }

        int paramCount = functionCompletion.getParamCount();
        for (int i = 0; i < paramCount; i++) {
            Parameter param = functionCompletion.getParam(i);
            String type = param.getType();
            String name = param.getName();

            if (StringUtils.isNotBlank(type)) {
                if (selected) {
                    builder.append(type);
                } else {
                    builder.append("<font color='");
                    builder.append(paramColor);
                    builder.append("'>");
                    builder.append(type);
                    builder.append("</font>");
                }

                if (StringUtils.isNotBlank(name)) {
                    builder.append(' ');
                }
            }

            if (StringUtils.isNotBlank(name)) {
                builder.append(name);
            }

            if (i < paramCount - 1) {
                builder.append(functionCompletion.getProvider().getParameterListSeparator());
            }
        }

        char paramListEnd = functionCompletion.getProvider().getParameterListEnd();
        if (paramListEnd != 0) {
            builder.append(paramListEnd);
        }

        if (getShowTypes() && StringUtils.isNotBlank(functionCompletion.getType())) {
            builder.append(" : ");

            if (selected) {
                builder.append(functionCompletion.getType());
            } else {
                builder.append("<font color='");
                builder.append(typeColor);
                builder.append("'>");
                builder.append(functionCompletion.getType());
                builder.append("</font>");
            }
        }

        setText(builder.toString());
    }

    private String createParamColor() {
        Color foreground = UIManager.getColor("Label.foreground");
        if (foreground == null) {
            foreground = new JLabel().getForeground();
        }
        Color hyperlinkForeground = Util.isLightForeground(foreground) ? new Color(0xd8ffff) : Color.BLUE;
        return Util.isLightForeground(getForeground()) ? Util.getHexString(hyperlinkForeground) : "#AA0077";
    }
}