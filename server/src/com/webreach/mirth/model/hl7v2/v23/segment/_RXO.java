package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXO extends Segment {
	public _RXO(){
		fields = new Class[]{_CE.class, _NM.class, _NM.class, _CE.class, _CE.class, _CE.class, _CE.class, _CM.class, _ID.class, _CE.class, _NM.class, _CE.class, _NM.class, _XCN.class, _XCN.class, _ID.class, _ST.class, _NM.class, _CE.class, _CE.class, _ST.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Requested Give Code", "Requested Give Amount - Minimum", "Requested Give Amount - Maximum", "Requested Give Units", "Requested Dosage Form", "Provider's Pharmacy/Treatment Instructions", "Provider's Administration Instructions", "Deliver-to Location", "Allow Substitutions", "Requested Dispense Code", "Requested Dispense Amount", "Requested Dispense Units", "Number of Refills", "Ordering Provider's DEA Number", "Pharmacist/Treatment Supplier's Verifier ID", "Needs Human Review", "Requested Give Per (Time Unit)", "Requested Give Strength", "Requested Give Strength Units", "Indication", "Requested Give Rate Amount", "Requested Give Rate Units"};
		description = "Pharmacy/Treatment prescription order";
		name = "RXO";
	}
}
