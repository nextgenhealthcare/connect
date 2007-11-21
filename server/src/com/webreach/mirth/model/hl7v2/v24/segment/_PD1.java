package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PD1 extends Segment {
	public _PD1(){
		fields = new Class[]{_IS.class, _IS.class, _XON.class, _XCN.class, _IS.class, _IS.class, _IS.class, _IS.class, _ID.class, _CX.class, _CE.class, _ID.class, _DT.class, _XON.class, _CE.class, _IS.class, _DT.class, _DT.class, _IS.class, _IS.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Living Dependency", "Living Arrangement", "Patient Primary Facility", "Patient Primary Care Provider Name & ID No.", "Student Indicator", "Handicap", "Living Will Code", "Organ Donor Code", "Separate Bill", "Duplicate Patient", "Publicity Code", "Protection Indicator", "Protection Indicator Effective Date", "Place of Worship", "Advance Directive Code", "Immunization Registry Status", "Immunization Registry Status Effective Date", "Publicity Code Effective Date", "Military Branch", "Military Rank/Grade", "Military Status"};
		description = "Patient Additional Demographic";
		name = "PD1";
	}
}
