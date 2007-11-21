package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXD extends Segment {
	public _RXD(){
		fields = new Class[]{_NM.class, _CE.class, _TS.class, _NM.class, _CE.class, _CE.class, _ST.class, _NM.class, _ST.class, _XCN.class, _ID.class, _CQ.class, _LA2.class, _ID.class, _CE.class, _NM.class, _CE.class, _ST.class, _TS.class, _CE.class, _CE.class, _NM.class, _CE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Dispense Sub-Id Counter", "Dispense/Give Code", "Date/Time Dispensed", "Actual Dispense Amount", "Actual Dispense Units", "Actual Dosage Form", "Prescription Number", "Number of Refills Remaining", "Dispense Notes", "Dispensing Provider", "Substitution Status", "Total Daily Dose", "Dispense-to Location", "Needs Human Review", "Pharmacy/Treatment Supplier’s Special Dispensing Instructions", "Actual Strength", "Actual Strength Unit", "Substance Lot Number", "Substance Expiration Date", "Substance Manufacturer Name", "Indication", "Dispense Package Size", "Dispense Package Size Unit", "Dispense Package Method"};
		description = "Pharmacy/Treatment Dispense";
		name = "RXD";
	}
}
