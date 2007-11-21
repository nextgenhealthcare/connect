package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RDERDE extends Message{	
	public _RDERDE(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _CTI.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, -1, -1, -1, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, false, true, true, false, true, true, false, true, true, false, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 10, 0, 1}, {3, 12, 0, 0}, {17, 18, 0, 0}, {14, 18, 0, 0}, {22, 23, 1, 1}, {13, 24, 1, 1}}; 
		description = "Pharmacy Encoded Order Message";
		name = "RDERDE";
	}
}
