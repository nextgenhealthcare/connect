package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BTX extends Segment {
	public _BTX(){
		fields = new Class[]{_SI.class, _EI.class, _CNE.class, _CNE.class, _CWE.class, _XON.class, _EI.class, _NM.class, _NM.class, _CE.class, _CWE.class, _ID.class, _TS.class, _XCN.class, _XCN.class, _TS.class, _TS.class, _CWE.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Donation ID", "Component", "Blood Group", "Commercial Product", "Manufacturer", "Lot Number", "Quantity", "Amount", "Units", "Transfusion/Disposition Status", "Message Status", "Date/Time of Status", "Administrator", "Verifier", "Transfusion Start Date/Time of Status", "Transfusion End Date/Time of Status", "Adverse Reaction Type", "Transfusion Interrupted Reason"};
		description = "Blood Product Transfusion/Disposition";
		name = "BTX";
	}
}
