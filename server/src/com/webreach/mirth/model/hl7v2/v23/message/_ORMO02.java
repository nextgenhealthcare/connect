package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORMO02 extends Message{	
	public _ORMO02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _ORC.class, _NTE.class, _CTI.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, 0, -1, -1};
		required = new boolean[]{true, true, false, false, true, false, true, false, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 10, 1, 1}, {5, 10, 0, 0}}; 
		description = "Order Refill Result";
		name = "ORMO02";
	}
}
