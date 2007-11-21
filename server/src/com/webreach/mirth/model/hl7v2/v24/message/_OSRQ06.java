package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OSRQ06 extends Message{	
	public _OSRQ06(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _OBR.class, _RQD.class, _RQ1.class, _RXO.class, _ODS.class, _ODT.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, false, true, false, true, false, true, true, true, true, true, true, true, false, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {9, 17, 1, 1}, {7, 17, 0, 0}}; 
		description = "Query For Order Status - Response";
		name = "OSRQ06";
	}
}
