package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _OSD extends Composite {
	public _OSD(){
		fields = new Class[]{_ID.class, _ST.class, _IS.class, _ST.class, _IS.class, _ST.class, _NM.class, _ST.class, _ID.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence/Results Flag", "Placer Order Number: Entity Identifier", "Placer Order Number: Namespace ID", "Filler Order Number: Entity Identifier", "Filler Order Number: Namespace ID", "Sequence Condition Value", "Maximum Number of Repeats", "Placer Order Number: Universal ID", "Placer Order Number: Universal ID Type", "Filler Order Number: Universal ID", "Filler Order Number: Universal ID Type"};
		description = "Order Sequence Definition";
		name = "OSD";
	}
}
