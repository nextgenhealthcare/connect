package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORR extends Message{	
	public _ORR(){
		segments = new Class[]{_MSH.class, _MSA.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _ORO.class, _OBR.class, _RX1.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, false, false, true, true, true, true, false};
		groups = new int[][]{{7, 9, 0, 1}, {6, 10, 1, 1}, {4, 10, 0, 0}}; 
		description = "No Description.";
		name = "ORR";
	}
}
