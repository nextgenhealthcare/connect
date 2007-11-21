package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RRGO16 extends Message{	
	public _RRGO16(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _TQ1.class, _TQ2.class, _RXG.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, -1, 0, 0, -1, 0, 0, -1, -1, -1};
		required = new boolean[]{true, true, false, false, false, true, false, true, true, false, true, true, false, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {9, 10, 0, 1}, {12, 13, 1, 1}, {11, 15, 0, 0}, {8, 15, 1, 1}, {6, 15, 0, 0}}; 
		description = "Pharmacy/Treatment Give Acknowledgment";
		name = "RRGO16";
	}
}
