package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORLO34 extends Message{	
	public _ORLO34(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _SPM.class, _OBX.class, _SAC.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _SPM.class, _SAC.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{true, true, false, false, false, true, true, false, false, true, true, false, true, true, false};
		groups = new int[][]{{11, 12, 0, 1}, {14, 15, 0, 1}, {13, 15, 0, 0}, {10, 15, 0, 1}, {7, 15, 1, 1}, {6, 15, 0, 1}}; 
		description = "Laboratory Order Response Message to a Multiple Order Related to Single Specimin";
		name = "ORLO34";
	}
}
