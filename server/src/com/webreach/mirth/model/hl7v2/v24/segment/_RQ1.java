package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQ1 extends Segment {
	public _RQ1(){
		fields = new Class[]{_ST.class, _CE.class, _ST.class, _CE.class, _ST.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Anticipated Price", "Manufacturer Identifier", "Manufacturer's Catalog", "Vendor ID", "Vendor Catalog", "Taxable", "Substitute Allowed"};
		description = "Requisition Detail-1";
		name = "RQ1";
	}
}
