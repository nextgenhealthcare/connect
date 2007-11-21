package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXA extends Segment {
	public _RXA(){
		fields = new Class[]{_NM.class, _NM.class, _TS.class, _TS.class, _CE.class, _NM.class, _CE.class, _CE.class, _CE.class, _XCN.class, _LA2.class, _ST.class, _NM.class, _CE.class, _ST.class, _TS.class, _CE.class, _CE.class, _CE.class, _ID.class, _ID.class, _TS.class, _NM.class, _CWE.class, _CWE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Give Sub-id Counter", "Administration Sub-id Counter", "Date/Time Start of Administration", "Date/Time End of Administration", "Administered Code", "Administered Amount", "Administered Units", "Administered Dosage Form", "Administration Notes", "Administering Provider", "Administered-at Location", "Administered Per (time Unit)", "Administered Strength", "Administered Strength Units", "Substance Lot Number", "Substance Expiration Date", "Substance Manufacturer Name", "Substance/Treatment Refusal Reason", "Indication", "Completion Status", "Action Code", "System Entry Date/Time", "Administered Drug Strength Volume", "Administered Drug Strength Volume Units", "Administered Barcode Identifier", "Pharmacy Order Type"};
		description = "Pharmacy/Treatment Administration";
		name = "RXA";
	}
}
