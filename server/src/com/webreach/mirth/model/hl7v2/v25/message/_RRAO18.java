package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RRAO18 extends Message{	
	public _RRAO18(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _TQ1.class, _TQ2.class, _RXA.class, _RXR.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, -1, 0, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, false, false, true, false, true, true, false, true, true};
		groups = new int[][]{{6, 7, 0, 0}, {9, 10, 0, 1}, {11, 12, 0, 0}, {8, 12, 1, 1}, {6, 12, 0, 0}}; 
		description = "Pharmacy/Treatment Administration Acknowledgment";
		name = "RRAO18";
	}
}
