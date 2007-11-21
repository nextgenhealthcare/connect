package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EANU09 extends Message{	
	public _EANU09(){
		segments = new Class[]{_MSH.class, _EQU.class, _NDS.class, _NTE.class, _ROL.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false};
		groups = new int[][]{{3, 4, 1, 1}}; 
		description = "Automated Equipment Notification";
		name = "EANU09";
	}
}
