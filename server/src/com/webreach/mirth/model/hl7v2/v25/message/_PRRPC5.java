package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRRPC5 extends Message{	
	public _PRRPC5(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _PID.class, _PV1.class, _PV2.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PTH.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _ANY.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, -1, 0, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, false, false, true, true, true, false, true, false, false, true, false, true, false, true, false, true, false, false, true, false, true, false, true, true, true, false, false, true, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {13, 14, 0, 1}, {15, 16, 0, 1}, {17, 18, 0, 1}, {22, 23, 0, 1}, {24, 25, 0, 1}, {19, 25, 0, 1}, {27, 28, 0, 1}, {31, 33, 0, 1}, {27, 33, 0, 0}, {26, 33, 0, 1}, {10, 33, 1, 1}, {7, 33, 1, 1}}; 
		description = "PC/ Problem Response";
		name = "PRRPC5";
	}
}
