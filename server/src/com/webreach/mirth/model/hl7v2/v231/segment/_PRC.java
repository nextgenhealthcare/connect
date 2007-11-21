package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRC extends Segment {
	public _PRC(){
		fields = new Class[]{_CE.class, _CE.class, _CE.class, _IS.class, _CP.class, _ST.class, _NM.class, _NM.class, _MO.class, _MO.class, _TS.class, _TS.class, _IS.class, _CE.class, _ID.class, _ID.class, _MO.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Facility ID", "Department", "Valid Patient Classes", "Price", "Formula", "Minimum Quantity", "Maximum Quantity", "Minimum Price", "Maximum Price", "Effective Start Date", "Effective End Date", "Price Override Flag", "Billing Category", "Chargeable Flag", "Active/Inactive Flag", "Cost", "Charge On Indicator"};
		description = "Pricing";
		name = "PRC";
	}
}
