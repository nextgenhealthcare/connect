package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IN3 extends Segment {
	public _IN3(){
		fields = new Class[]{_SI.class, _CX.class, _XCN.class, _ID.class, _CM.class, _TS.class, _TS.class, _XCN.class, _DT.class, _DT.class, _CM.class, _CE.class, _TS.class, _XCN.class, _ST.class, _XTN.class, _CE.class, _CE.class, _XTN.class, _CM.class, _ST.class, _DT.class, _IS.class, _IS.class, _XCN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Certification Number", "Certified By", "Certification Required", "Penalty", "Certification Date/Time", "Certification Modify Date/Time", "Operator", "Certification Begin Date", "Certification End Date", "Days", "Non-Concur Code/Description", "Non-Concur Effective Date/Time", "Physician Reviewer", "Certification Contact", "Certification Contact Phone Number", "Appeal Reason", "Certification Agency", "Certification Agency Phone Number", "Pre-Certification Req/Window", "Case Manager", "Second Opinion Date", "Second Opinion Status", "Second Opinion Documentation Received", "Second Opinion Physician"};
		description = "Insurance additional information, certification";
		name = "IN3";
	}
}
