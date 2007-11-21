package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXA extends Segment {
	public _RXA(){
		fields = new Class[]{_NM.class, _NM.class, _TS.class, _TS.class, _CE.class, _NM.class, _CE.class, _CE.class, _CE.class, _XCN.class, _LA2.class, _ST.class, _NM.class, _CE.class, _ST.class, _TS.class, _CE.class, _CE.class, _CE.class, _ID.class, _ID.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Give Sub-ID Counter", "Administration Sub-ID Counter", "Date/Time Start of Administration", "Date/Time End of Administration", "Administered Code", "Administered Amount", "Administered Units", "Administered Dosage Form", "Administration Notes", "Administering Provider", "Administered-at Location", "Administered Per (Time Unit)", "Administered Strength", "Administered Strength Units", "Substance Lot Number", "Substance Expiration Date", "Substance Manufacturer Name", "Substance/Treatment Refusal Reason", "Indication", "Completion Status", "Action Code", "System Entry Date/Time"};
		description = "Pharmacy/Treatment Administration";
		name = "RXA";
	}
}
