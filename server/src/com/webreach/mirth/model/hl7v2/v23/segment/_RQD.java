package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQD extends Segment {
	public _RQD(){
		fields = new Class[]{_SI.class, _CE.class, _CE.class, _CE.class, _NM.class, _CE.class, _IS.class, _IS.class, _CE.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Requisition Line Number", "Item Code - Internal", "Item Code - External", "Hospital Item Code", "Requisition Quantity", "Requisition Unit of Measure", "Dept. Cost Center", "Item Natural Account Code", "Deliver to ID", "Date Needed"};
		description = "Requisition Detail";
		name = "RQD";
	}
}
