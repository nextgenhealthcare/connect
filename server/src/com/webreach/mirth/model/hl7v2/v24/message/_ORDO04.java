package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORDO04 extends Message{	
	public _ORDO04(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _ODS.class, _NTE.class, _ORC.class, _ODT.class, _NTE.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, false, false, true, false, true, false, false, true, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 9, 1, 1}, {10, 12, 0, 1}, {5, 12, 0, 0}}; 
		description = "Diet Order Acknowledgement - Response";
		name = "ORDO04";
	}
}
