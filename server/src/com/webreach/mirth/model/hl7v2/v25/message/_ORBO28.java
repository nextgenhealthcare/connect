package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORBO28 extends Message{	
	public _ORBO28(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _ORC.class, _TQ1.class, _TQ2.class, _BPO.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, false, false, true, true, true, false, false};
		groups = new int[][]{{8, 9, 0, 1}, {7, 10, 0, 1}, {6, 10, 0, 1}}; 
		description = "Blood Product Order Acknowledgment";
		name = "ORBO28";
	}
}
