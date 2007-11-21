package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _ED extends Composite {
	public _ED(){
		fields = new Class[]{_HD.class, _ID.class, _ID.class, _ID.class, _TX.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Source Application", "Type of Data", "Data Subtype", "Encoding", "Data"};
		description = "Encapsulated Data";
		name = "ED";
	}
}
