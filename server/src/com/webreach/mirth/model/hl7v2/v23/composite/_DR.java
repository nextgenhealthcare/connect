package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DR extends Composite {
	public _DR(){
		fields = new Class[]{_TS.class, _TS.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Range Start Date/Time", "Range End Date/Time"};
		description = "Date/Time Range";
		name = "DR";
	}
}
