package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRA extends Segment {
	public _PRA(){
		fields = new Class[]{_CE.class, _CE.class, _IS.class, _ID.class, _SPD.class, _PLN.class, _PIP.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Practitioner Group", "Practitioner Category", "Provider Billing", "Specialty", "Practitioner ID Numbers", "Privileges", "Date Entered Practice"};
		description = "Practitioner Detail";
		name = "PRA";
	}
}
