package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PV1 extends Segment {
	public _PV1(){
		fields = new Class[]{_SI.class, _IS.class, _PL.class, _IS.class, _CX.class, _PL.class, _XCN.class, _XCN.class, _XCN.class, _IS.class, _PL.class, _IS.class, _IS.class, _IS.class, _IS.class, _IS.class, _XCN.class, _IS.class, _CX.class, _FC.class, _IS.class, _IS.class, _IS.class, _IS.class, _DT.class, _NM.class, _NM.class, _IS.class, _IS.class, _DT.class, _IS.class, _NM.class, _NM.class, _IS.class, _DT.class, _IS.class, _DLD.class, _CE.class, _IS.class, _IS.class, _IS.class, _PL.class, _PL.class, _TS.class, _TS.class, _NM.class, _NM.class, _NM.class, _NM.class, _CX.class, _IS.class, _XCN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Patient Class", "Assigned Patient Location", "Admission Type", "Preadmit Number", "Prior Patient Location", "Attending Doctor", "Referring Doctor", "Consulting Doctor", "Hospital Service", "Temporary Location", "Preadmit Test Indicator", "Re-admission Indicator", "Admit Source", "Ambulatory Status", "VIP Indicator", "Admitting Doctor", "Patient Type", "Visit Number", "Financial Class", "Charge Price Indicator", "Courtesy Code", "Credit Rating", "Contract Code", "Contract Effective Date", "Contract Amount", "Contract Period", "Interest Code", "Transfer to Bad Debt Code", "Transfer to Bad Debt Date", "Bad Debt Agency Code", "Bad Debt Transfer Amount", "Bad Debt Recovery Amount", "Delete Account Indicator", "Delete Account Date", "Discharge Disposition", "Discharged to Location", "Diet Type", "Servicing Facility", "Bed Status", "Account Status", "Pending Location", "Prior Temporary Location", "Admit Date/Time", "Discharge Date/Time", "Current Patient Balance", "Total Charges", "Total Adjustments", "Total Payments", "Alternate Visit ID", "Visit Indicator", "Other Healthcare Provider"};
		description = "Patient visit";
		name = "PV1";
	}
}
