package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PT extends Composite {
	public _PT(){
		fields = new Class[]{_ID.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Processing Type", "Processing Mode"};
		description = "Processing Type";
		name = "PT";
	}
}
