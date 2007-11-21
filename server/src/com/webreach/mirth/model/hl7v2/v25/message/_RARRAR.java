package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RARRAR extends Message{	
	public _RARRAR(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _RXE.class, _RXR.class, _RXC.class, _RXA.class, _RXR.class, _DSC.class};
		repeats = new int[]{0, 0, -1, -1, 0, 0, 0, -1, 0, 0, -1, -1, -1, 0, 0};
		required = new boolean[]{true, true, false, false, true, false, true, false, true, true, true, false, true, true, false};
		groups = new int[][]{{7, 8, 0, 0}, {10, 12, 0, 0}, {9, 14, 1, 1}, {5, 14, 1, 1}}; 
		description = "Pharmacy/Treatment Administration Information - Response";
		name = "RARRAR";
	}
}
