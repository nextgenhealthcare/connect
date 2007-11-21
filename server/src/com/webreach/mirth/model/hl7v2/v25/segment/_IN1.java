package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IN1 extends Segment {
	public _IN1(){
		fields = new Class[]{_SI.class, _CE.class, _CX.class, _XON.class, _XAD.class, _XPN.class, _XTN.class, _ST.class, _XON.class, _CX.class, _XON.class, _DT.class, _DT.class, _AUI.class, _IS.class, _XPN.class, _CE.class, _TS.class, _XAD.class, _IS.class, _IS.class, _ST.class, _ID.class, _DT.class, _ID.class, _DT.class, _IS.class, _ST.class, _TS.class, _XCN.class, _IS.class, _IS.class, _NM.class, _NM.class, _IS.class, _ST.class, _CP.class, _CP.class, _NM.class, _CP.class, _CP.class, _CE.class, _IS.class, _XAD.class, _ST.class, _IS.class, _IS.class, _IS.class, _CX.class, _IS.class, _DT.class, _ST.class, _IS.class};
		repeats = new int[]{0, 0, -1, -1, -1, -1, -1, 0, -1, -1, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Insurance Plan ID", "Insurance Company ID", "Insurance Company Name", "Insurance Company Address", "Insurance Co Contact Person", "Insurance Co Phone Number", "Group Number", "Group Name", "Insured's Group Emp ID", "Insured's Group Emp Name", "Plan Effective Date", "Plan Expiration Date", "Authorization Information", "Plan Type", "Name of Insured", "Insured's Relationship to Patient", "Insured's Date of Birth", "Insured's Address", "Assignment of Benefits", "Coordination of Benefits", "Coord of Ben. Priority", "Notice of Admission Flag", "Notice of Admission Date", "Report of Eligibility Flag", "Report of Eligibility Date", "Release Information Code", "Pre-Admit Cert (pac)", "Verification Date/Time", "Verification By", "Type of Agreement Code", "Billing Status", "Lifetime Reserve Days", "Delay Before L.r. Day", "Company Plan Code", "Policy Number", "Policy Deductible", "Policy Limit - Amount", "Policy Limit - Days", "Room Rate - Semi-private", "Room Rate - Private", "Insured's Employment Status", "Insured's Administrative Sex", "Insured's Employer's Address", "Verification Status", "Prior Insurance Plan ID", "Coverage Type", "Handicap", "Insured's ID Number", "Signature Code", "Signature Code Date", "Insured's Birth Place", "Vip Indicator"};
		description = "Insurance";
		name = "IN1";
	}
}
