package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _HD extends Composite {
	public _HD(){
		fields = new Class[]{_IS.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Namespace ID", "Universal ID", "Universal ID Type"};
		description = "Hierarchic Designator";
		name = "HD";
	}
}
