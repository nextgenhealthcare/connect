package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IN1 extends Segment {
	public _IN1(){
		fields = new Class[]{_SI.class, _ID.class, _ST.class, _ST.class, _AD.class, _PN.class, _TN.class, _ST.class, _ST.class, _ST.class, _ST.class, _DT.class, _DT.class, _CM.class, _ID.class, _PN.class, _ID.class, _DT.class, _AD.class, _ID.class, _ID.class, _ST.class, _ST.class, _DT.class, _ID.class, _DT.class, _ID.class, _ST.class, _TS.class, _CN.class, _ID.class, _ID.class, _NM.class, _NM.class, _ID.class, _ST.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _CE.class, _ID.class, _AD.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Insurance Plan ID", "Insurance Company ID", "Insurance Company Name", "Insurance Company Address", "Insurance Co. Contact Pers", "Insurance Co Phone Number", "Group Number", "Group Name", "Insured's Group Emp ID", "Insured's Group Emp Name", "Plan Effective Date", "Plan Expiration Date", "Authorization Information", "Plan Type", "Name of Insured", "Insured's Relationship to Patient", "Insured's Date of Birth", "Insured's Address", "Assignment of Benefits", "Coordination of Benefits", "Coord of Ben. Priority", "Notice of Admission Code", "Notice of Admission Date", "Rpt of Eligibility Code", "Rpt of Eligibility Date", "Release Information Code", "Pre-Admit Cert (Pac)", "Verification Date/Time", "Verification By", "Type of Agreement Code", "Billing Status", "Lifetime Reserve Days", "Delay Before L. R. Day", "Company Plan Code", "Policy Number", "Policy Deductible", "Policy Limit - Amount", "Policy Limit - Days", "Room Rate - Semi-Private", "Room Rate - Private", "Insured's Employment Status", "Insured's Sex", "Insured's Employer Address", "Verification Status", "Prior Insurance Plan ID"};
		description = "Insurance";
		name = "IN1";
	}
}
