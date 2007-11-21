package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _URS extends Segment {
	public _URS(){
		fields = new Class[]{_ST.class, _TS.class, _TS.class, _ST.class, _ST.class};
		repeats = new int[]{-1, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"R/U Where Subject Def.", "R/U When Start Dte/Tme", "R/U When End Dte/Tme", "R/U What User Qualifier", "R/U Oth Results Def."};
		description = "Unsolicited Selection";
		name = "URS";
	}
}
