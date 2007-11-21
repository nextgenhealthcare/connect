package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORDO04 extends Message{	
	public _ORDO04(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _TQ1.class, _TQ2.class, _ODS.class, _NTE.class, _ORC.class, _TQ1.class, _TQ2.class, _ODT.class, _NTE.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, -1, 0, 0, -1, -1, -1, 0, 0, -1, -1, -1};
		required = new boolean[]{true, true, false, false, false, true, false, true, true, false, false, false, true, true, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {9, 10, 0, 1}, {8, 12, 1, 1}, {14, 15, 0, 1}, {13, 17, 0, 1}, {6, 17, 0, 0}}; 
		description = "Diet Order Acknowledgment";
		name = "ORDO04";
	}
}
