package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ROL extends Segment {
	public _ROL(){
		fields = new Class[]{_EI.class, _ID.class, _CE.class, _XCN.class, _TS.class, _TS.class, _CE.class, _CE.class, _CE.class, _CE.class, _XAD.class, _XTN.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, 0, -1, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Role Instance ID", "Action Code", "Role", "Role Person", "Role Begin Date/Time", "Role End Date/Time", "Role Duration", "Role Action Reason", "Provider Type", "Organization Unit Type", "Office/Home Address/Birthplace", "Phone"};
		description = "Role";
		name = "ROL";
	}
}
