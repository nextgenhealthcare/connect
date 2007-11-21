package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORPO10 extends Message{	
	public _ORPO10(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, 0, -1, -1, -1, -1};
		required = new boolean[]{true, true, false, false, true, false, true, true, false, true, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {8, 12, 0, 0}, {7, 12, 1, 1}, {5, 12, 0, 0}}; 
		description = "Pharmacy/Treatment Order Acknowledgement - Response";
		name = "ORPO10";
	}
}
