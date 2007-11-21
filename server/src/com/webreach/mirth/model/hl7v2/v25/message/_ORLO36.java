package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORLO36 extends Message{	
	public _ORLO36(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _SPM.class, _OBX.class, _SAC.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, -1, 0, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, false, false, true, true, false, true, true, true, false, true};
		groups = new int[][]{{11, 12, 0, 1}, {10, 13, 0, 1}, {9, 13, 1, 1}, {7, 13, 1, 1}, {6, 13, 0, 1}}; 
		description = "Laboratory Order Response Message to a Single Container of a Specimen OML";
		name = "ORLO36";
	}
}
