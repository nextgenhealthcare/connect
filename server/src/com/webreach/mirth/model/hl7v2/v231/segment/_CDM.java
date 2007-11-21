package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CDM extends Segment {
	public _CDM(){
		fields = new Class[]{_CE.class, _CE.class, _ST.class, _ST.class, _IS.class, _CE.class, _CE.class, _ID.class, _CE.class, _NM.class, _CK.class, _XON.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Charge Code Alias", "Charge Description Short", "Charge Description Long", "Description Override Indicator", "Exploding Charges", "Procedure Code", "Active/Inactive Flag", "Inventory Number", "Resource Load", "Contract Number", "Contract Organization", "Room Fee Indicator"};
		description = "Charge Description Master";
		name = "CDM";
	}
}
