package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NK1 extends Segment {
	public _NK1(){
		fields = new Class[]{_SI.class, _PN.class, _ST.class, _AD.class, _TN.class};
		repeats = new int[]{0, 0, 0, 0, -1};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Next of Kin Name", "Next of Kin Relationship", "Next of Kin Address", "Next of Kin Phone Number"};
		description = "Next of Kin/Associated Parties";
		name = "NK1";
	}
}
