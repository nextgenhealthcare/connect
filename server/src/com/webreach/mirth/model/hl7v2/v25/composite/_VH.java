package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _VH extends Composite {
	public _VH(){
		fields = new Class[]{_ID.class, _ID.class, _TM.class, _TM.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Start Day Range", "End Day Range", "Start Hour Range", "End Hour Range"};
		description = "Visiting Hours";
		name = "VH";
	}
}
