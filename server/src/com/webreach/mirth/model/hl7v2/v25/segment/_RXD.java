package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXD extends Segment {
	public _RXD(){
		fields = new Class[]{_NM.class, _CE.class, _TS.class, _NM.class, _CE.class, _CE.class, _ST.class, _NM.class, _ST.class, _XCN.class, _ID.class, _CQ.class, _LA2.class, _ID.class, _CE.class, _NM.class, _CE.class, _ST.class, _TS.class, _CE.class, _CE.class, _NM.class, _CE.class, _ID.class, _CE.class, _CE.class, _CE.class, _NM.class, _CWE.class, _CWE.class, _XAD.class, _ID.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, 0, -1, -1, -1, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Dispense Sub-id Counter", "Dispense/Give Code", "Date/Time Dispensed", "Actual Dispense Amount", "Actual Dispense Units", "Actual Dosage Form", "Prescription Number", "Number of Refills Remaining", "Dispense Notes", "Dispensing Provider", "Substitution Status", "Total Daily Dose", "Dispense-to Location", "Needs Human Review", "Pharmacy/Treatment Supplier's Special Dispensing Instructions", "Actual Strength", "Actual Strength Unit", "Substance Lot Number", "Substance Expiration Date", "Substance Manufacturer Name", "Indication", "Dispense Package Size", "Dispense Package Size Unit", "Dispense Package Method", "Supplementary Code", "Initiating Location", "Packaging/Assembly Location", "Actual Drug Strength Volume", "Actual Drug Strength Volume Units", "Dispense to Pharmacy", "Dispense to Pharmacy Address", "Pharmacy Order Type", "Dispense Type"};
		description = "Pharmacy/Treatment Dispense";
		name = "RXD";
	}
}
