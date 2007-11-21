package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ88 extends Message{	
	public _RSPZ88(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RCP.class, _PID.class, _PD1.class, _NTE.class, _AL1.class, _PV1.class, _PV2.class, _ORC.class, _TQ1.class, _TQ2.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class, _RXD.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, -1, -1, -1, -1, 0, 0, -1, -1, -1, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, true, true, true, true, false, false, true, true, false, true, true, false, true, false, true, true, false, true, true, false, true, false, true, true, false, false, false, true};
		groups = new int[][]{{12, 13, 0, 0}, {11, 13, 0, 0}, {8, 13, 0, 0}, {15, 16, 0, 1}, {20, 21, 0, 0}, {17, 21, 0, 0}, {23, 24, 0, 1}, {22, 26, 0, 0}, {30, 31, 1, 1}, {14, 31, 1, 1}, {8, 31, 1, 1}}; 
		description = "Dispense Information (response)";
		name = "RSPZ88";
	}
}
