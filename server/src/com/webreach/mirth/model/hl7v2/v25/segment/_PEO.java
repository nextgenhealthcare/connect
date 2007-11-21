package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PEO extends Segment {
	public _PEO(){
		fields = new Class[]{_CE.class, _CE.class, _TS.class, _TS.class, _TS.class, _TS.class, _XAD.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _FT.class, _FT.class, _FT.class, _FT.class, _FT.class, _CE.class, _XPN.class, _XAD.class, _XTN.class, _ID.class, _ID.class, _TS.class, _ID.class};
		repeats = new int[]{-1, -1, 0, 0, 0, 0, -1, -1, 0, 0, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Event Identifiers Used", "Event Symptom/Diagnosis Code", "Event Onset Date/Time", "Event Exacerbation Date/Time", "Event Improved Date/Time", "Event Ended Data/Time", "Event Location Occurred Address", "Event Qualification", "Event Serious", "Event Expected", "Event Outcome", "Patient Outcome", "Event Description From Others", "Event From Original Reporter", "Event Description From Patient", "Event Description From Practitioner", "Event Description From Autopsy", "Cause of Death", "Primary Observer Name", "Primary Observer Address", "Primary Observer Telephone", "Primary Observer's Qualification", "Confirmation Provided By", "Primary Observer Aware Date/Time", "Primary Observer's Identity May Be Divulged"};
		description = "Product Experience Observation";
		name = "PEO";
	}
}
