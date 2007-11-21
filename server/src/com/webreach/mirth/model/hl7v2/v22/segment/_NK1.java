package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NK1 extends Segment {
	public _NK1(){
		fields = new Class[]{_SI.class, _PN.class, _CE.class, _AD.class, _TN.class, _TN.class, _CE.class, _DT.class, _DT.class, _ST.class, _CM.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Name", "Relationship", "Address", "Phone Number", "Business Phone Number", "Contact Role", "Start Date", "End Date", "Next of Kin Job Title", "Next of Kin Job Code/Class", "Next of Kin Employee Number", "Organization Name"};
		description = "Next of Kin/Associated Parties";
		name = "NK1";
	}
}
