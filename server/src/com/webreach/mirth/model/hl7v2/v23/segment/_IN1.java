package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IN1 extends Segment {
	public _IN1(){
		fields = new Class[]{_SI.class, _CE.class, _CX.class, _XON.class, _XAD.class, _XPN.class, _XTN.class, _ST.class, _XON.class, _CX.class, _XON.class, _DT.class, _DT.class, _CM.class, _IS.class, _XPN.class, _IS.class, _TS.class, _XAD.class, _IS.class, _IS.class, _ST.class, _ID.class, _DT.class, _ID.class, _DT.class, _IS.class, _ST.class, _TS.class, _XCN.class, _IS.class, _IS.class, _NM.class, _NM.class, _IS.class, _ST.class, _CP.class, _CP.class, _NM.class, _CP.class, _CP.class, _CE.class, _IS.class, _XAD.class, _ST.class, _IS.class, _IS.class, _IS.class, _CX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Insurance Plan ID", "Insurance Company ID", "Insurance Company Name", "Insurance Company Address", "Insurance Co. Contact Person", "Insurance Co Phone Number", "Group Number", "Group Name", "Insured's Group Emp ID", "Insured's Group Emp Name", "Plan Effective Date", "Plan Expiration Date", "Authorization Information", "Plan Type", "Name of Insured", "Insured's Relationship to Patient", "Insured's Date of Birth", "Insured's Address", "Assignment of Benefits", "Coordination of Benefits", "Coordination of Benefits Priority", "Notice of Admission Flag", "Notice of Admission Date", "Rpt of Eligibility Flag", "Rpt of Eligibility Date", "Release Information Code", "Pre-Admit Cert (PAC)", "Verification Date/Time", "Verification By", "Type of Agreement Code", "Billing Status", "Lifetime Reserve Days", "Delay Before L. R. Day", "Company Plan Code", "Policy Number", "Policy Deductible", "Policy Limit - Amount", "Policy Limit - Days", "Room Rate - Semi-Private", "Room Rate - Private", "Insured's Employment Status", "Insured's Sex", "Insured's Employer Address", "Verification Status", "Prior Insurance Plan ID", "Coverage Type", "Handicap", "Insured's ID Number"};
		description = "Insurance";
		name = "IN1";
	}
}
