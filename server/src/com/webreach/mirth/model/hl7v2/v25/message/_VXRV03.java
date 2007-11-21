package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VXRV03 extends Message{	
	public _VXRV03(){
		segments = new Class[]{_MSH.class, _MSA.class, _SFT.class, _QRD.class, _QRF.class, _PID.class, _PD1.class, _NK1.class, _PV1.class, _PV2.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ORC.class, _TQ1.class, _TQ2.class, _RXA.class, _RXR.class, _OBX.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, true, false, true, false, false, true, false, false, true, false, false, true, true, false, true, false, true, false};
		groups = new int[][]{{9, 10, 0, 0}, {12, 14, 0, 1}, {16, 17, 0, 1}, {20, 21, 0, 1}, {15, 21, 0, 1}}; 
		description = "Vaccination Record Response";
		name = "VXRV03";
	}
}
