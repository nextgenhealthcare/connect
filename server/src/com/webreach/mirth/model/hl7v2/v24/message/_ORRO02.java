package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORRO02 extends Message{	
	public _ORRO02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _OBR.class, _RQD.class, _RQ1.class, _RXO.class, _ODS.class, _ODT.class, _NTE.class, _CTI.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1};
		required = new boolean[]{true, true, false, false, true, false, true, true, true, true, true, true, true, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 15, 1, 1}, {5, 15, 0, 0}}; 
		description = "Order Response";
		name = "ORRO02";
	}
}
