package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BPX extends Segment {
	public _BPX(){
		fields = new Class[]{_SI.class, _CWE.class, _ID.class, _TS.class, _EI.class, _CNE.class, _CNE.class, _CWE.class, _XON.class, _EI.class, _CNE.class, _CNE.class, _TS.class, _NM.class, _NM.class, _CE.class, _EI.class, _PL.class, _XAD.class, _XCN.class, _XCN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Dispense Status", "Status", "Date/Time of Status", "Donation ID", "Component", "Donation Type/Intended Use", "Commercial Product", "Manufacturer", "Lot Number", "Blood Group", "Special Testing", "Expiration Date/Time", "Quantity", "Amount", "Units", "Unique ID", "Actual Dispensed to Location", "Actual Dispensed to Address", "Dispensed to Receiver", "Dispensing Individual"};
		description = "Blood Product Dispense Status";
		name = "BPX";
	}
}
