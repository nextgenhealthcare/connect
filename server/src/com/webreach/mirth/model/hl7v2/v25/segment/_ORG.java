package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORG extends Segment {
	public _ORG(){
		fields = new Class[]{_SI.class, _CE.class, _CE.class, _ID.class, _CX.class, _CE.class, _CE.class, _CE.class, _DR.class, _CE.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Organization Unit Code", "Organization Unit Type Code", "Primary Org Unit Indicator", "Practitioner Org Unit Identifier", "Health Care Provider Type Code", "Health Care Provider Classification Code", "Health Care Provider Area of Specialization Code", "Effective Date Range", "Employment Status Code", "Board Approval Indicator", "Primary Care Physician Indicator"};
		description = "Practitioner Organization Unit";
		name = "ORG";
	}
}
