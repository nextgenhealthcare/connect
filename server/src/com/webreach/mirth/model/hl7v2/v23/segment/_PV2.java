package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PV2 extends Segment {
	public _PV2(){
		fields = new Class[]{_PL.class, _CE.class, _CE.class, _CE.class, _ST.class, _ST.class, _IS.class, _TS.class, _TS.class, _NM.class, _NM.class, _ST.class, _XCN.class, _DT.class, _ID.class, _IS.class, _DT.class, _IS.class, _ID.class, _NM.class, _IS.class, _ID.class, _XON.class, _IS.class, _IS.class, _DT.class, _IS.class, _DT.class, _DT.class, _IS.class, _IS.class, _ID.class, _TS.class, _ID.class, _ID.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Prior Pending Location", "Accommodation Code", "Admit Reason", "Transfer Reason", "Patient Valuables", "Patient Valuables Location", "Visit User Code", "Expected Admit Date/Time", "Expected Discharge Date/Time", "Estimated Length of Inpatient Stay", "Actual Length of Inpatient Stay", "Visit Description", "Referral Source Code", "Previous Service Date", "Employment Illness Related Indicator", "Purge Status Code", "Purge Status Date", "Special Program Code", "Retention Indicator", "Expected Number of Insurance Plans", "Visit Publicity Code", "Visit Protection Indicator", "Clinic Organization Name", "Patient Status Code", "Visit Priority Code", "Previous Treatment Date", "Expected Discharge Disposition", "Signature on File Date", "First Similar Illness Date", "Patient Charge Adjustment Code", "Recurring Service Code", "Billing Media Code", "Expected Surgery Date & Time", "Military Partnership Code", "Military Non-Availabiltiy Code", "Newborn Baby Indicator", "Baby Detained Indicator"};
		description = "Patient Visit - Additional Information";
		name = "PV2";
	}
}
