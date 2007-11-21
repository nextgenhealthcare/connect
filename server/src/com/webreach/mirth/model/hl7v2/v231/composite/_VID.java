package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _VID extends Composite {
	public _VID(){
		fields = new Class[]{_ID.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Version ID", "Internationalization Code", "International Version ID"};
		description = "Version Identifier";
		name = "VID";
	}
}
