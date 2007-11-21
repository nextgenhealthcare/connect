package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IIM extends Segment {
	public _IIM(){
		fields = new Class[]{_CWE.class, _CWE.class, _ST.class, _TS.class, _CWE.class, _CWE.class, _TS.class, _NM.class, _CWE.class, _MO.class, _TS.class, _NM.class, _CWE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Service Item Code", "Inventory Lot Number", "Inventory Expiration Date", "Inventory Manufacturer Name", "Inventory Location", "Inventory Received Date", "Inventory Received Quantity", "Inventory Received Quantity Unit", "Inventory Received Item Cost", "Inventory On Hand Date", "Inventory On Hand Quantity", "Inventory On Hand Quantity Unit", "Procedure Code", "Procedure Code Modifier"};
		description = "Inventory Item Master";
		name = "IIM";
	}
}
