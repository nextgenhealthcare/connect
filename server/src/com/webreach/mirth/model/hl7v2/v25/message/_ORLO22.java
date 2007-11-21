package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORLO22 extends Message{	
	public _ORLO22(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _SPM.class, _SAC.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{true, true, false, false, false, true, true, true, false, true, true, false};
		groups = new int[][]{{8, 9, 0, 1}, {11, 12, 0, 1}, {10, 12, 0, 0}, {7, 12, 0, 1}, {6, 12, 0, 1}}; 
		description = "General Laboratory Order Response Message to Any OML";
		name = "ORLO22";
	}
}
