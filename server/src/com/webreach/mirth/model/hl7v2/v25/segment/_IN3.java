package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IN3 extends Segment {
	public _IN3(){
		fields = new Class[]{_SI.class, _CX.class, _XCN.class, _ID.class, _MOP.class, _TS.class, _TS.class, _XCN.class, _DT.class, _DT.class, _DTN.class, _CE.class, _TS.class, _XCN.class, _ST.class, _XTN.class, _CE.class, _CE.class, _XTN.class, _ICD.class, _ST.class, _DT.class, _IS.class, _IS.class, _XCN.class};
		repeats = new int[]{0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1, -1, 0, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Certification Number", "Certified By", "Certification Required", "Penalty", "Certification Date/Time", "Certification Modify Date/Time", "Operator", "Certification Begin Date", "Certification End Date", "Days", "Non-concur Code/Description", "Non-concur Effective Date/Time", "Physician Reviewer", "Certification Contact", "Certification Contact Phone Number", "Appeal Reason", "Certification Agency", "Certification Agency Phone Number", "Pre-certification Requirement", "Case Manager", "Second Opinion Date", "Second Opinion Status", "Second Opinion Documentation Received", "Second Opinion Physician"};
		description = "Insurance Additional Information, Certification";
		name = "IN3";
	}
}
