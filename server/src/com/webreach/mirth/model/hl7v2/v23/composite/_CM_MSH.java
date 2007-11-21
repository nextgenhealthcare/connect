package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CM_MSH extends Composite {
	public _CM_MSH(){
		fields = new Class[]{_ID.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Type", "Event"};
		description = "MSH Event-Type Composite";
		name = "CM_MSH";
	}
}
