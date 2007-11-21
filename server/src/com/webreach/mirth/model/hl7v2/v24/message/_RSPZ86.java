package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ86 extends Message{	
	public _RSPZ86(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _PID.class, _PD1.class, _NTE.class, _AL1.class, _ORC.class, _RXO.class, _RXR.class, _RXC.class, _RXE.class, _RXR.class, _RXC.class, _RXD.class, _RXR.class, _RXC.class, _RXG.class, _RXR.class, _RXC.class, _RXA.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{true, true, false, true, true, true, false, false, false, true, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, false, false, false};
		groups = new int[][]{{11, 13, 0, 0}, {14, 16, 0, 0}, {17, 19, 0, 0}, {20, 22, 0, 0}, {23, 25, 0, 0}, {26, 27, 1, 1}, {10, 27, 1, 1}, {6, 27, 0, 1}}; 
		description = "Pharmacy Information Comprehensive (response)";
		name = "RSPZ86";
	}
}
