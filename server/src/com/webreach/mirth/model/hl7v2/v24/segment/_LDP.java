package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LDP extends Segment {
	public _LDP(){
		fields = new Class[]{_PL.class, _CE.class, _IS.class, _CE.class, _IS.class, _ID.class, _TS.class, _TS.class, _ST.class, _VH.class, _XTN.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Location Department", "Location Service", "Specialty Type", "Valid Patient Classes", "Active/Inactive Flag", "Activation Date  LDP", "Inactivation Date", "Inactivated Reason", "Visiting Hours", "Contact Phone", "Location Cost Center"};
		description = "Location Department";
		name = "LDP";
	}
}
