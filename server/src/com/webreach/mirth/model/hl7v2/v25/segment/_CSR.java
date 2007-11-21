package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CSR extends Segment {
	public _CSR(){
		fields = new Class[]{_EI.class, _EI.class, _CE.class, _CX.class, _CX.class, _TS.class, _XCN.class, _XCN.class, _TS.class, _CE.class, _TS.class, _CE.class, _CE.class, _CE.class, _TS.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, -1, 0, 0, -1, -1, -1, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sponsor Study ID", "Alternate Study ID", "Institution Registering the Patient", "Sponsor Patient ID", "Alternate Patient ID", "Date/Time of Patient Study Registration", "Person Performing Study Registration", "Study Authorizing Provider", "Date/Time Patient Study Consent Signed", "Patient Study Eligibility Status", "Study Randomization Date/Time", "Randomized Study Arm", "Stratum For Study Randomization", "Patient Evaluability Status", "Date/Time Ended Study", "Reason Ended Study"};
		description = "Clinical Study Registration";
		name = "CSR";
	}
}
