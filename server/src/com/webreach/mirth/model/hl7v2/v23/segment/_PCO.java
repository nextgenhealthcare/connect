package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCO extends Segment {
	public _PCO(){
		fields = new Class[]{_XON.class, _CX.class, _IS.class, _XPN.class, _ST.class, _DT.class, _DT.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _DT.class, _XAD.class, _XTN.class, _XTN.class, _ST.class, _IS.class, _IS.class, _OH.class, _IS.class, _IS.class, _PPN.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _XAD.class, _XTN.class, _XTN.class, _XAD.class, _XTN.class, _XTN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Office ID (internal)", "External ID", "Classification ID", "Office Manager", "CLIA Certification Number", "CLIA expiration date", "Office review date", "Is CLIA wavier", "Is solo", "Is primary", "Has handicap access", "Is phone 24 hours", "Date joined", "Address", "Phone", "Fax", "Comment", "Standing ID", "Practicing Specialties", "Office Hours", "Provider Type", "Line of Business", "Revised By", "Electronic Claims", "Accepting Patients", "Assistant Present", "Surgery", "Anesthesia Class", "Office Languages", "AgeLimitation", "AgeComment", "ConfidentialFax", "BillingAddress", "BillingPhone", "BillingFax", "CorrespondenceAddress", "CorrespondencePhone", "CorrespondenceFax"};
		description = "Office Information Segment";
		name = "PCO";
	}
}
