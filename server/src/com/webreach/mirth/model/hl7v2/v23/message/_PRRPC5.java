package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRRPC5 extends Message{	
	public _PRRPC5(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _PID.class, _PV1.class, _PV2.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PTH.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _OBR.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, false, true, true, true, false, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false, true, true, true, false, false, true, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {11, 12, 0, 1}, {13, 14, 0, 1}, {15, 16, 0, 1}, {19, 20, 0, 1}, {21, 22, 0, 1}, {17, 22, 0, 1}, {28, 30, 0, 1}, {24, 30, 0, 0}, {23, 30, 0, 1}, {8, 30, 1, 1}, {5, 30, 1, 1}}; 
		description = "Patient Problem Query Response";
		name = "PRRPC5";
	}
}
