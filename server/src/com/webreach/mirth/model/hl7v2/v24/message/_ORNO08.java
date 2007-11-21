package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORNO08 extends Message{	
	public _ORNO08(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _RQD.class, _RQ1.class, _NTE.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, false, true, false, true, true, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 10, 1, 1}, {5, 10, 0, 0}}; 
		description = "Non-stock Requisition Acknowledgement - Response";
		name = "ORNO08";
	}
}
