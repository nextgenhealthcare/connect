package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IN2 extends Segment {
	public _IN2(){
		fields = new Class[]{_ST.class, _NM.class, _CN.class, _ID.class, _ID.class, _NM.class, _PN.class, _NM.class, _PN.class, _NM.class, _ID.class, _ST.class, _ST.class, _ID.class, _ID.class, _ID.class, _DT.class, _ID.class, _ID.class, _ID.class, _ST.class, _PN.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class, _CM.class, _CM.class, _CM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Insured's Employee ID", "Insured's Social Security Number", "Insured's Employer Name", "Employer Information Data", "Mail Claim Party", "Medicare Health Ins Card Number", "Medicaid Case Name", "Medicaid Case Number", "Champus Sponsor Name", "Champus ID Number", "Dependent of Champus Recipient", "Champus Organization", "Champus Station", "Champus Service", "Champus Rank/Grade", "Champus Status", "Champus Retire Date", "Champus Non-Avail Cert On File", "Baby Coverage", "Combine Baby Bill", "Blood Deductible", "Special Coverage Approval Name", "Special Coverage Approval Title", "Non-Covered Insurance Code", "Payor ID", "Payor Subscriber ID", "Eligibility Source", "Room Coverage Type/Amount", "Policy Type/Amount", "Daily Deductible"};
		description = "Insurance Additional Information";
		name = "IN2";
	}
}
