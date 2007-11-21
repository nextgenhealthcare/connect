package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CON extends Segment {
	public _CON(){
		fields = new Class[]{_SI.class, _CWE.class, _ST.class, _EI.class, _FT.class, _FT.class, _FT.class, _FT.class, _FT.class, _CNE.class, _CNE.class, _TS.class, _TS.class, _TS.class, _TS.class, _ID.class, _ID.class, _ID.class, _ID.class, _CWE.class, _ID.class, _CWE.class, _CWE.class, _XPN.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Consent Type", "Consent Form ID", "Consent Form Number", "Consent Text", "Subject-specific Consent Text", "Consent Background", "Subject-specific Consent Background", "Consenter-imposed Limitations", "Consent Mode", "Consent Status", "Consent Discussion Date/Time", "Consent Decision Date/Time", "Consent Effective Date/Time", "Consent End Date/Time", "Subject Competence Indicator", "Translator Assistance Indicator", "Language Translated To", "Informational Material Supplied Indicator", "Consent Bypass Reason", "Consent Disclosure Level", "Consent Non-disclosure Reason", "Non-subject Consenter Reason", "Consenter ID", "Relationship to Subject Table"};
		description = "Consent Segment";
		name = "CON";
	}
}
