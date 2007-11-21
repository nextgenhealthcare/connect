package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPTPCL extends Message{	
	public _PPTPCL(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _PID.class, _PV1.class, _PV2.class, _PTH.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _OBR.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class, _VAR.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, 0, -1, -1, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, true, true, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, true, true, true, false, false, true, false, false, true};
		groups = new int[][]{{6, 7, 0, 0}, {11, 12, 0, 1}, {16, 17, 0, 1}, {18, 19, 0, 1}, {23, 24, 0, 1}, {25, 26, 0, 1}, {20, 26, 0, 1}, {32, 35, 0, 1}, {28, 35, 0, 0}, {27, 35, 0, 1}, {13, 35, 0, 1}, {8, 35, 1, 1}, {5, 35, 1, 1}}; 
		description = "Patient Pathway (Goal-Oriented) Response";
		name = "PPTPCL";
	}
}
