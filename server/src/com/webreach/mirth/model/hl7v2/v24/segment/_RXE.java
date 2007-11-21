package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXE extends Segment {
	public _RXE(){
		fields = new Class[]{_TQ.class, _CE.class, _NM.class, _NM.class, _CE.class, _CE.class, _CE.class, _LA1.class, _ID.class, _NM.class, _CE.class, _NM.class, _XCN.class, _XCN.class, _ST.class, _NM.class, _NM.class, _TS.class, _CQ.class, _ID.class, _CE.class, _ST.class, _ST.class, _CE.class, _NM.class, _CE.class, _CE.class, _NM.class, _CE.class, _ID.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Quantity/Timing", "Give Code", "Give Amount - Minimum", "Give Amount - Maximum", "Give Units", "Give Dosage Form", "Provider's Administration Instructions", "Deliver-To Location", "Substitution Status", "Dispense Amount", "Dispense Units", "Number of Refills", "Ordering Provider's DEA Number", "Pharmacist/Treatment Supplier's Verifier ID", "Prescription Number", "Number of Refills Remaining", "Number of Refills/Doses Dispensed", "D/T of Most Recent Refill or Dose Dispensed", "Total Daily Dose", "Needs Human Review", "Pharmacy/Treatment Supplier's Special Dispensing Instructions", "Give Per (Time Unit)", "Give Rate Amount", "Give Rate Units", "Give Strength", "Give Strength Units", "Give Indication", "Dispense Package Size", "Dispense Package Size Unit", "Dispense Package Method", "Supplementary Code"};
		description = "Pharmacy/Treatment Encoded Order";
		name = "RXE";
	}
}
