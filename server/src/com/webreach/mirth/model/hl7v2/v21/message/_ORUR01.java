package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORUR01 extends Message{	
	public _ORUR01(){
		segments = new Class[]{_MSH.class, _MSA.class, _PID.class, _NTE.class, _PV1.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, true, true, false, false, false, true, false, false, false, false};
		groups = new int[][]{{3, 5, 0, 0}, {9, 10, 1, 1}, {6, 10, 1, 1}, {3, 10, 1, 1}}; 
		description = "[P,F] Observations to Follow";
		name = "ORUR01";
	}
}
