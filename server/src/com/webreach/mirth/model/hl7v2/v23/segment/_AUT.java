package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _AUT extends Segment {
	public _AUT(){
		fields = new Class[]{_CE.class, _CE.class, _ST.class, _TS.class, _TS.class, _EI.class, _CP.class, _NM.class, _NM.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Authorizing Payor, Plan ID", "Authorizing Payor, Company ID", "Authorizing Payor, Company Name", "Authorization Effective Date", "Authorization Expiration Date", "Authorization Identifier", "Reimbursement Limit", "Requested Number of Treatments", "Authorized Number of Treatments", "Process Date"};
		description = "Authorization Information";
		name = "AUT";
	}
}
