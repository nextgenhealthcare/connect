package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXG extends Segment {
	public _RXG(){
		fields = new Class[]{_NM.class, _NM.class, _TQ.class, _CE.class, _NM.class, _NM.class, _CE.class, _CE.class, _CE.class, _ID.class, _LA2.class, _ID.class, _CE.class, _ST.class, _ST.class, _CE.class, _NM.class, _CE.class, _ST.class, _TS.class, _CE.class, _CE.class, _NM.class, _CWE.class, _CWE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Give Sub-id Counter", "Dispense Sub-id Counter", "Quantity/Timing", "Give Code", "Give Amount - Minimum", "Give Amount - Maximum", "Give Units", "Give Dosage Form", "Administration Notes", "Substitution Status", "Dispense-to Location", "Needs Human Review", "Pharmacy/Treatment Supplier's Special Administration Instructions", "Give Per (time Unit)", "Give Rate Amount", "Give Rate Units", "Give Strength", "Give Strength Units", "Substance Lot Number", "Substance Expiration Date", "Substance Manufacturer Name", "Indication", "Give Drug Strength Volume", "Give Drug Strength Volume Units", "Give Barcode Identifier", "Pharmacy Order Type"};
		description = "Pharmacy/Treatment Give";
		name = "RXG";
	}
}
