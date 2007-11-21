package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORLO22 extends Message{	
	public _ORLO22(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _SAC.class, _OBX.class, _ORC.class, _OBR.class, _SAC.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{true, true, false, false, true, true, false, true, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {9, 10, 0, 0}, {8, 10, 0, 1}, {6, 10, 1, 1}, {5, 10, 0, 1}}; 
		description = "General Laboratory Order Acknowledgment Message - Response";
		name = "ORLO22";
	}
}
