package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _URD extends Segment {
	public _URD(){
		fields = new Class[]{_TS.class, _ID.class, _ST.class, _ID.class, _ST.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, -1, -1, -1, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"R/U Date/Time", "Report Priority", "R/U Who Subject Definition", "R/U What Subject Definition", "R/U What Department Code", "R/U Display/Print Locations", "R/U Results Level"};
		description = "Results/Update Definition";
		name = "URD";
	}
}
