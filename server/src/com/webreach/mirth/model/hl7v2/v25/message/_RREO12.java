package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RREO12 extends Message{	
	public _RREO12(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _TQ1.class, _TQ2.class, _RXE.class, _NTE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, -1, -1};
		required = new boolean[]{true, true, false, false, false, true, false, true, true, false, true, false, true, false, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {9, 10, 0, 1}, {13, 14, 1, 1}, {11, 16, 0, 0}, {8, 16, 1, 1}, {6, 16, 0, 0}}; 
		description = "Pharmacy/Treatment Encoded Order Acknowledgment";
		name = "RREO12";
	}
}
