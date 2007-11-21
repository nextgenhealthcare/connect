package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ82 extends Message{	
	public _RSPZ82(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RCP.class, _PID.class, _PD1.class, _NTE.class, _AL1.class, _PV1.class, _PV2.class, _ORC.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _RXR.class, _RXC.class, _RXD.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{true, true, false, true, true, true, true, false, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, false, false, false};
		groups = new int[][]{{11, 12, 0, 0}, {10, 12, 0, 0}, {17, 18, 0, 0}, {14, 18, 0, 0}, {19, 21, 0, 0}, {25, 26, 1, 1}, {13, 26, 1, 1}, {7, 26, 0, 1}}; 
		description = "Dispense History (response)";
		name = "RSPZ82";
	}
}
