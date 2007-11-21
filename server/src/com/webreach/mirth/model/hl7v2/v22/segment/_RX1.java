package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RX1 extends Segment {
	public _RX1(){
		fields = new Class[]{_UN.class, _UN.class, _ST.class, _ST.class, _CQ.class, _CQ.class, _NM.class, _NM.class, _CM.class, _ID.class, _NM.class, _CQ.class, _UN.class, _CE.class, _ID.class, _ID.class, _ID.class, _ID.class, _NM.class, _UN.class, _NM.class, _ID.class, _NM.class, _UN.class, _TS.class, _ST.class, _ID.class, _TX.class, _TX.class, _TX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Unused", "Unused", "Route", "Site Administered", "IV Solution Rate", "Drug Strength", "Final Concentration", "Final Volume in Ml.", "Drug Dose", "Drug Role", "Prescription Sequence Num", "Quantity Dispensed", "Unused", "Drug ID", "Component Drug IDs", "Prescription Type", "Substitution Status", "Rx Order Status", "Number of Refills", "Unused", "Refills Remaining", "DEA Class", "Ordering Md's DEA Number", "Unused", "Last Refill Date/Time", "RX Number", "PRN Status", "Pharmacy Instructions", "Patient Instruction", "Instructions (SIG)"};
		description = "No description";
		name = "RX1";
	}
}
