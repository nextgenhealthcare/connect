package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RASO17 extends Message{	
	public _RASO17(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _AL1.class, _PV1.class, _PV2.class, _ORC.class, _TQ1.class, _TQ2.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class, _RXA.class, _RXR.class, _OBX.class, _NTE.class, _CTI.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1, -1, -1, 0, 0, -1, -1};
		required = new boolean[]{true, false, false, true, false, false, false, true, false, true, true, false, true, true, true, true, false, true, true, false, true, false, true, true, true, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {4, 9, 0, 0}, {11, 12, 0, 1}, {16, 17, 0, 1}, {14, 17, 0, 0}, {13, 17, 0, 0}, {19, 20, 1, 1}, {18, 22, 0, 0}, {25, 26, 0, 1}, {23, 26, 1, 1}, {10, 27, 1, 1}}; 
		description = "Pharmacy/Treatment Administration";
		name = "RASO17";
	}
}
