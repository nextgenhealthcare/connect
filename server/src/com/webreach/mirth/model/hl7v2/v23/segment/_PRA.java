package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRA extends Segment {
	public _PRA(){
		fields = new Class[]{_ST.class, _CE.class, _IS.class, _ID.class, _CM.class, _CM.class, _CM.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Practioner Group", "Practioner Category", "Provider Billing", "Specialty", "Practitioner ID Numbers", "Privileges", "Date Entered Practice"};
		description = "Pracitioner Detail";
		name = "PRA";
	}
}
