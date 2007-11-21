package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DTM extends Composite {
	public _DTM(){
		fields = new Class[]{_DateTime.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Value"};
		description = "Date/Time";
		name = "DTM";
	}
}
