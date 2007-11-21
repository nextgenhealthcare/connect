package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OSRQ06 extends Message{	
	public _OSRQ06(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _RQD.class, _RQ1.class, _RXO.class, _ODS.class, _ODT.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, false, false, true, false, true, false, true, true, false, true, true, true, true, true, true, false, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {11, 12, 0, 1}, {13, 18, 0, 1}, {10, 20, 1, 1}, {8, 20, 0, 0}}; 
		description = "Query For Order Status - Response";
		name = "OSRQ06";
	}
}
