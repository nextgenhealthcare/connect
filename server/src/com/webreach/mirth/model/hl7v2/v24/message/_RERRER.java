package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RERRER extends Message{	
	public _RERRER(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _RXE.class, _RXR.class, _RXC.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, true, false, true, false, true, true, true, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 11, 1, 1}, {4, 11, 1, 1}}; 
		description = "Pharmacy Encoded Order Information Query Response";
		name = "RERRER";
	}
}
