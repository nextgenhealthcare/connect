package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _URD extends Segment {
	public _URD(){
		fields = new Class[]{_SI.class, _CK.class, _CK.class, _ST.class, _PN.class, _ST.class, _DT.class};
		repeats = new int[]{0, 0, -1, -1, -1, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"R/U Date/Time", "Report Priority", "R/U Who Subject Defnition", "R/U What Subject Defntion", "R/U What Department Code", "R/U Display/Print Locs", "R/U Results Level"};
		description = "Results/Update Definition";
		name = "URD";
	}
}
