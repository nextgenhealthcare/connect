package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXG extends Segment {
	public _RXG(){
		fields = new Class[]{_NM.class, _NM.class, _TQ.class, _CE.class, _NM.class, _NM.class, _CE.class, _CE.class, _CE.class, _ID.class, _LA2.class, _ID.class, _CE.class, _ST.class, _ST.class, _CE.class, _NM.class, _CE.class, _ST.class, _TS.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Give Sub-Id Counter", "Dispense Sub-id Counter", "Quantity/Timing", "Give Code", "Give Amount - Minimum", "Give Amount - Maximum", "Give Units", "Give Dosage Form", "Administration Notes", "Substitution Status", "Dispense-to Location", "Needs Human Review", "Pharmacy/Treatment Supplier’s Special Administration Instructions", "Give Per (Time Unit)", "Give Rate Amount", "Give Rate Units", "Give Strength", "Give Strength Units", "Substance Lot Number", "Substance Expiration Date", "Substance Manufacturer Name", "Indication"};
		description = "Pharmacy/Treatment Give";
		name = "RXG";
	}
}
