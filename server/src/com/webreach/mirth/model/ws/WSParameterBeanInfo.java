package com.webreach.mirth.model.ws;

import com.l2fprod.common.beans.BaseBeanInfo;

public class WSParameterBeanInfo extends BaseBeanInfo {

	public WSParameterBeanInfo() {
		super(WSParameter.class);
		addProperty("name").setCategory("General").setReadOnly();
		addProperty("type").setCategory("General").setReadOnly();
		addProperty("value").setCategory("General");
	}
}