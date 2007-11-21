package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XCN extends Composite {
	public _XCN(){
		fields = new Class[]{_ST.class, _FN.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _IS.class, _HD.class, _ID.class, _ST.class, _ID.class, _ID.class, _HD.class, _ID.class, _CE.class, _DR.class, _ID.class, _TS.class, _TS.class, _ST.class, _CWE.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number", "Family Name", "Given Name", "Second and Further Given Names or Initials Thereof", "Suffix", "Prefix", "Degree", "Source Table", "Assigning Authority", "Name Type Code", "Identifier Check Digit", "Check Digit Scheme", "Identifier Type Code", "Assigning Facility", "Name Representation Code", "Name Context", "Name Validity Range", "Name Assembly Order", "Effective Date", "Expiration Date", "Professional Suffix", "Assigning Jurisdiction", "Assigning Agency or Department"};
		description = "Extended Composite ID Number and Name For Persons";
		name = "XCN";
	}
}
