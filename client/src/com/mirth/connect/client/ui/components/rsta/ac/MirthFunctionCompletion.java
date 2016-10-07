/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.rsta.ac.js.IconFactory;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import com.mirth.connect.client.ui.reference.FunctionReference;
import com.mirth.connect.client.ui.reference.Reference;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.util.MirthXmlUtil;

public abstract class MirthFunctionCompletion extends FunctionCompletion implements MirthCompletion {

    protected String id;
    protected CodeTemplateContextSet contextSet;
    protected List<Parameter> parameters;
    protected String iconName;
    protected boolean deprecated;

    public MirthFunctionCompletion(CompletionProvider provider, FunctionReference reference) {
        super(provider, reference.getFunctionDefinition().getName(), MirthXmlUtil.encode(StringUtils.defaultString(reference.getFunctionDefinition().getReturnType())));
        this.id = reference.getId();
        this.contextSet = reference.getContextSet();
        setShortDescription(reference.getDescription());
        setReturnValueDescription(reference.getFunctionDefinition().getReturnDescription());

        List<Parameter> list = new ArrayList<Parameter>();
        if (CollectionUtils.isNotEmpty(reference.getFunctionDefinition().getParameters())) {
            for (com.mirth.connect.model.Parameter param : reference.getFunctionDefinition().getParameters()) {
                Parameter parameter = new Parameter(param.getType(), param.getName());
                parameter.setDescription(param.getDescription());
                list.add(parameter);
            }
        }
        setParams(list);

        this.iconName = reference.getIconName();
        this.deprecated = reference.isDeprecated();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CodeTemplateContextSet getContextSet() {
        return contextSet;
    }

    public List<Parameter> getParams() {
        return parameters;
    }

    @Override
    public void setParams(List<Parameter> params) {
        if (params != null) {
            parameters = new ArrayList<Parameter>(params);
            super.setParams(params);
        }
    }

    @Override
    public Icon getIcon() {
        if (StringUtils.isNotBlank(iconName)) {
            return IconFactory.getIcon(iconName);
        }
        return IconFactory.getIcon(IconFactory.PUBLIC_METHOD_ICON);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    @Override
    public int getRelevance() {
        return 100;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MirthCompletion) {
            return ((MirthCompletion) obj).getId().equals(id);
        } else if (obj instanceof Reference) {
            return ((Reference) obj).getId().equals(id);
        }
        return false;
    }

    @Override
    public int compareTo(Completion c2) {
        int result = compare(getReplacementText(), c2.getReplacementText());
        if (result != 0) {
            return result;
        }

        if (c2 instanceof ParameterizedCompletion) {
            ParameterizedCompletion completion = (ParameterizedCompletion) c2;
            result = compare(getParamCount(), completion.getParamCount());
            if (result != 0) {
                return result;
            }

            for (int i = 0; i < getParamCount(); i++) {
                Parameter param1 = getParam(i);
                Parameter param2 = completion.getParam(i);
                result = compare(param1.getName(), param2.getName());
                if (result != 0) {
                    return result;
                }

                result = compare(param1.getType(), param2.getType());
                if (result != 0) {
                    return result;
                }
            }
        }

        return super.compareTo(c2);
    }

    private <T> int compare(Comparable<T> obj1, T obj2) {
        if (obj1 == null) {
            if (obj2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (obj2 == null) {
            return 1;
        } else {
            return obj1.compareTo(obj2);
        }
    }
}