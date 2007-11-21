package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXE extends Segment {
	public _RXE(){
		fields = new Class[]{_TQ.class, _CE.class, _NM.class, _NM.class, _CE.class, _CE.class, _CE.class, _LA1.class, _ID.class, _NM.class, _CE.class, _NM.class, _XCN.class, _XCN.class, _ST.class, _NM.class, _NM.class, _TS.class, _CQ.class, _ID.class, _CE.class, _ST.class, _ST.class, _CE.class, _NM.class, _CE.class, _CE.class, _NM.class, _CE.class, _ID.class, _CE.class, _TS.class, _NM.class, _CWE.class, _CWE.class, _ID.class, _CWE.class, _CWE.class, _NM.class, _CWE.class, _XAD.class, _PL.class, _XAD.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Quantity/Timing", "Give Code", "Give Amount - Minimum", "Give Amount - Maximum", "Give Units", "Give Dosage Form", "Provider's Administration Instructions", "Deliver-to Location", "Substitution Status", "Dispense Amount", "Dispense Units", "Number of Refills", "Ordering Provider's Dea Number", "Pharmacist/Treatment Supplier's Verifier ID", "Prescription Number", "Number of Refills Remaining", "Number of Refills/Doses Dispensed", "D/T of Most Recent Refill or Dose Dispensed", "Total Daily Dose", "Needs Human Review", "Pharmacy/Treatment Supplier's Special Dispensing Instructions", "Give Per (time Unit)", "Give Rate Amount", "Give Rate Units", "Give Strength", "Give Strength Units", "Give Indication", "Dispense Package Size", "Dispense Package Size Unit", "Dispense Package Method", "Supplementary Code", "Original Order Date/Time", "Give Drug Strength Volume", "Give Drug Strength Volume Units", "Controlled Substance Schedule", "Formulary Status", "Pharmaceutical Substance Alternative", "Pharmacy of Most Recent Fill", "Initial Dispense Amount", "Dispensing Pharmacy", "Dispensing Pharmacy Address", "Deliver-to Patient Location", "Deliver-to Address", "Pharmacy Order Type"};
		description = "Pharmacy/Treatment Encoded Order";
		name = "RXE";
	}
}
