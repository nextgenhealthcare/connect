package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PD1 extends Segment {
	public _PD1(){
		fields = new Class[]{_IS.class, _IS.class, _XON.class, _XCN.class, _IS.class, _IS.class, _IS.class, _IS.class, _ID.class, _CX.class, _CE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Living Dependency", "Living Arrangement", "Patient Primary Facility", "Patient Primary Care Provider Name & ID No.", "Student Indicator", "Handicap", "Living Will", "Organ Donor", "Separate Bill", "Duplicate Patient", "Publicity Indicator", "Protection Indicator"};
		description = "Patient Demographic";
		name = "PD1";
	}
}
